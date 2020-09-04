package com.jxin.faas.scheduler.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.google.common.collect.Lists;
import com.jxin.faas.scheduler.infrastructure.util.IdUtil;
import com.jxin.faas.scheduler.repository.persistence.IContainerRepository;
import com.jxin.faas.scheduler.repository.persistence.IFuncRepository;
import com.jxin.faas.scheduler.repository.persistence.INodeRepository;
import com.jxin.faas.scheduler.repository.table.Container;
import com.jxin.faas.scheduler.repository.table.Func;
import com.jxin.faas.scheduler.repository.table.Node;
import com.jxin.faas.scheduler.service.IContainerManager;
import com.jxin.faas.scheduler.service.INodeManager;
import com.jxin.faas.scheduler.service.ISchedulerService;
import com.jxin.faas.scheduler.service.acl.nodeservice.INodeServiceAcl;
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
    @Value("${containerManager.maxDelayCleanTime}")
    private Integer maxDelayCleanTime;
    @Value("${containerManager.expandDivisor}")
    private BigDecimal expandDivisor;



    private final IContainerRepository containerRepository;
    private final IFuncRepository funcRepository;
    private final INodeRepository nodeRepository;

    private final INodeServiceAcl nodeServiceAcl;

    private final INodeManager nodeManager;
    @Autowired
    public ContainerManager(IContainerRepository containerRepository, IFuncRepository funcRepository, INodeServiceAcl nodeServiceAcl, INodeRepository nodeRepository, INodeManager nodeManager) {
        this.containerRepository = containerRepository;
        this.funcRepository = funcRepository;
        this.nodeServiceAcl = nodeServiceAcl;
        this.nodeRepository = nodeRepository;
        this.nodeManager = nodeManager;
    }


    @Async("scaleExecutor")
    @Override
    public void scaleContainerByFunc(String funcName) {
        final int count = containerRepository.countByFuncName(funcName);
        final int scaleCount = NumberUtil.mul(expandDivisor, count).intValue();
        final Optional<Func> funcOptional = funcRepository.getFunc(funcName);
        if(!funcOptional.isPresent()){
            return;
        }
        final Func func = funcOptional.get();
        for (int i = 0; i < scaleCount; i++) {
            final Optional<Container> containerOpt = getContainerByOldNode(IdUtil.getRequestId(), func);
            if(!containerOpt.isPresent()){
                // 本地资源不足以创建就停手吧
                return;
            }
            final Container container = containerOpt.get();
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

    /**
     *
     * 根据已有的node获取新容器
     * @param  requestId 请求Id
     * @param  func      函数
     * @return 新容器
     */
    public Optional<Container> getContainerByOldNode(String requestId, Func func) {
        // 如果已有容器不足以支撑请求,则发起一次异步扩容
        scaleContainerByFunc(func.getName());
        final Optional<Node> availableNodeOptional = nodeManager.getAndUseNode(func.getMemorySize());

        if(availableNodeOptional.isPresent()){
            final Node node = availableNodeOptional.get();
            final Optional<Container> containerOpt = nodeServiceAcl.createContainer(requestId, node, func);
            if(containerOpt.isPresent()){
                final Container container = containerOpt.get();
                container.setEnabled(true);
                container.setOuttime(DateUtil.offsetMillisecond(new Date(), func.getTimeout()));
                containerRepository.save(container);
                return containerOpt;
            }
            return getContainerByOldNode(requestId, func);
        }
        return Optional.empty();
    }
    //******************************************************************************************************************
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
}
