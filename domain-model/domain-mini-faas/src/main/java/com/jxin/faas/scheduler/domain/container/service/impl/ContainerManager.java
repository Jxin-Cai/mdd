package com.jxin.faas.scheduler.domain.container.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.google.common.collect.Lists;
import com.jxin.faas.scheduler.domain.container.dmo.entity.Container;
import com.jxin.faas.scheduler.domain.container.dmo.val.FuncVal;
import com.jxin.faas.scheduler.domain.container.dmo.val.NodeVal;
import com.jxin.faas.scheduler.domain.container.factory.IContainerFactory;
import com.jxin.faas.scheduler.domain.container.repository.persistence.IContainerRepository;
import com.jxin.faas.scheduler.domain.container.repository.persistence.IFuncRepository;
import com.jxin.faas.scheduler.domain.container.repository.persistence.INodeRepository;
import com.jxin.faas.scheduler.domain.container.repository.table.ContainerDO;
import com.jxin.faas.scheduler.domain.container.repository.table.FuncDO;
import com.jxin.faas.scheduler.domain.container.service.IContainerManager;
import com.jxin.faas.scheduler.domain.container.service.INodeManager;
import com.jxin.faas.scheduler.domain.container.service.acl.nodeservice.INodeServiceAcl;
import com.jxin.faas.scheduler.infrastructure.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * 容器管理器 实现
 * @author Jxin
 * @version 1.0
 * @since 2020/9/4 19:54
 */
@Service
@Slf4j
public class ContainerManager implements IContainerManager {
    /**清理容器限流器*/
    private static final Semaphore CLEAN_CONTAINER_LOCK = new Semaphore(1);
    /**创建新节点的锁*/
    private static final Object NEW_NODE_LOCK = new Object();
    @Value("${containerManager.maxDelayCleanTime}")
    private Integer maxDelayCleanTime;
    @Value("${containerManager.expandDivisor}")
    private BigDecimal expandDivisor;

    private final IContainerRepository containerRepository;
    private final IFuncRepository funcRepository;
    private final INodeRepository nodeRepository;
    private final IContainerFactory containerFactory;
    private final INodeServiceAcl nodeServiceAcl;

    private final INodeManager nodeManager;
    @Autowired
    public ContainerManager(IContainerRepository containerRepository, IFuncRepository funcRepository, INodeServiceAcl nodeServiceAcl, INodeRepository nodeRepository, INodeManager nodeManager, IContainerFactory containerFactory) {
        this.containerRepository = containerRepository;
        this.funcRepository = funcRepository;
        this.nodeServiceAcl = nodeServiceAcl;
        this.nodeRepository = nodeRepository;
        this.nodeManager = nodeManager;
        this.containerFactory = containerFactory;
    }

    /**
     *
     * 从本地已有资源获取容器
     * @param  requestId 请求Id
     * @param  funcVal   函数我值对象
     * @return 容器
     */
    @Override
    public Optional<Container> getContainerByLocal(String requestId, FuncVal funcVal) {
        // 从已有的容器中获取
        final Optional<ContainerDO> containerDOOpt =
                containerRepository.getAndUseContainer(funcVal.getName(), DateUtil.offsetMillisecond(new Date(), funcVal.getTimeout()));
        return containerDOOpt.map(containerFactory::createContainer);

    }

    @Override
    public Optional<Container> getAndSaveContainerByOldNode(String requestId, FuncVal funcVal) {
        final Optional<Container> containerOpt = getContainerByOldNode(requestId, funcVal);
        if(!containerOpt.isPresent()){
            return Optional.empty();
        }
        final Container container = containerOpt.get();
        container.useContainer();
        containerRepository.save(containerFactory.createContainerDO(container));
        return containerOpt;
    }
    /**
     *
     * 1.获取新的节点来获取新的容器(本地数据存储和远程接口事物存在分布式事物,先不处理)
     * 2.同一时间只允许一个请求创建新的node
     * @param  requestId 请求Id
     * @param  func      函数
     * @return 容器
     */
    @Override
    public Container getAndSaveContainerByNewNode(String requestId, FuncVal func) {
        synchronized (NEW_NODE_LOCK){
            final Optional<Container> containerByLocalOpt = getContainerByLocal(requestId, func);
            if(containerByLocalOpt.isPresent()){
                return containerByLocalOpt.get();
            }
            // 获取新的节点来获取新的容器(本地数据存储和远程接口事物存在分布式事物,先不处理)
            nodeManager.reserveAndSaveNode();
            return getAndSaveContainerByNewNode(requestId, func);
        }
    }

    @Override
    public void finishRunJob(String requestId, String containerId, boolean success) {
        containerRepository.releaseContainer(containerId);
    }

    @Async("scaleExecutor")
    @Override
    public void scaleContainerByFunc(String funcName) {
        final int count = containerRepository.countByFuncName(funcName);
        final int scaleCount = NumberUtil.mul(expandDivisor, count).intValue();
        final Optional<FuncDO> funcOptional = funcRepository.getFunc(funcName);
        if(!funcOptional.isPresent()){
            return;
        }
        final FuncDO func = funcOptional.get();
        for (int i = 0; i < scaleCount; i++) {
            final Optional<Container> containerOpt = getContainerByOldNode(IdUtil.getRequestId(), FuncVal.of(func));
            if(!containerOpt.isPresent()){
                // 本地资源不足以创建就停手吧
                return;
            }
            final ContainerDO container = containerFactory.createContainerDO(containerOpt.get());
            container.setEnabled(false);
            containerRepository.save(container);
        }
    }

    @Override
    public void releaseContainer() {
        if(!CLEAN_CONTAINER_LOCK.tryAcquire()){
            return;
        }
        final List<String> nodeIdList = nodeRepository.nodeIdList();
        if(CollectionUtils.isEmpty(nodeIdList)){
            return;
        }
        nodeIdList.forEach(this::releaseContainerByNode);
    }
    //***********************************************common*************************************************************

    /**
     *
     * 根据已有的node获取新容器
     * @param  requestId 请求Id
     * @param  funcVal   函数值对象
     * @return 新容器
     */
    private Optional<Container> getContainerByOldNode(String requestId, FuncVal funcVal) {
        final Optional<NodeVal> availableNodeOptional = nodeManager.getAndUseNode(funcVal.getMemorySize());

        if(availableNodeOptional.isPresent()){
            final NodeVal node = availableNodeOptional.get();
            final Optional<Container> containerOpt = nodeServiceAcl.createContainer(requestId, node, funcVal);
            if(containerOpt.isPresent()){
                return containerOpt;
            }
            return getContainerByOldNode(requestId, funcVal);
        }
        return Optional.empty();
    }
    //********************************************releaseContainer******************************************************
    /**
     * 清理单个节点上的容器
     * @param nodeId 节点id
     */
    private void releaseContainerByNode(String nodeId) {
        final DateTime delayCleanTime = DateUtil.offsetSecond(new Date(), maxDelayCleanTime);
        final List<String> idleContainerIdList = containerRepository.removeIdleContainer(nodeId, delayCleanTime);
        final List<String> outTimeContainerIdList = containerRepository.removeOutTimeContainer(nodeId, new Date());
        if(CollectionUtils.isEmpty(idleContainerIdList) && CollectionUtils.isEmpty(outTimeContainerIdList)){
            return;
        }

        final List<String> containerIdList =
                Lists.newArrayListWithCapacity(idleContainerIdList.size() + outTimeContainerIdList.size());
        containerIdList.addAll(idleContainerIdList);
        containerIdList.addAll(outTimeContainerIdList);

        final CountDownLatch latch = new CountDownLatch(containerIdList.size());
        containerIdList.forEach(containerId -> nodeServiceAcl.removeContainer(IdUtil.getRequestId(), nodeId, containerId, latch));
        try {
            latch.await();
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }
    //********************************************getContainer**********************************************************

}
