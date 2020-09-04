package com.jxin.faas.scheduler.service.impl;

import com.jxin.faas.scheduler.infrastructure.util.IdUtil;
import com.jxin.faas.scheduler.repository.persistence.IContainerRepository;
import com.jxin.faas.scheduler.repository.persistence.INodeRepository;
import com.jxin.faas.scheduler.repository.table.Node;
import com.jxin.faas.scheduler.service.INodeManager;
import com.jxin.faas.scheduler.service.acl.nodeservice.INodeServiceAcl;
import com.jxin.faas.scheduler.service.acl.resourcemanager.IResourceManagerAcl;
import com.jxin.faas.scheduler.service.entity.NodeStatVal;
import com.jxin.faas.scheduler.service.exec.NodeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 节点管理器
 * @author Jxin
 * @version 1.0
 * @since 2020/9/4 22:56
 */
@Service
@Slf4j
public class NodeManager implements INodeManager {
    /**扩容节点限流器*/
    private static final Semaphore SCALE_NODE_LOCK = new Semaphore(1);
    /**系统用户Id*/
    private static final String SYS_ACCOUNT_ID = "s";
    @Value("${nodeManager.maxCpuUsageRatio}")
    private BigDecimal maxCpuUsageRatio;
    @Value("${nodeManager.maxMemUsageRatio}")
    private BigDecimal maxMemUsageRatio;
    @Value("${nodeManager.scaleCount}")
    private Integer scaleCount;
    @Value("${nodeManager.maxCpuRate}")
    private BigDecimal maxCpuRate;

    private final INodeRepository nodeRepository;
    private final IContainerRepository containerRepository;

    private final IResourceManagerAcl resourceManagerAcl;
    private final INodeServiceAcl nodeServiceAcl;

    @Autowired
    public NodeManager(INodeRepository nodeRepository, IContainerRepository containerRepository, IResourceManagerAcl resourceManagerAcl, INodeServiceAcl nodeServiceAcl) {
        this.nodeRepository = nodeRepository;
        this.containerRepository = containerRepository;
        this.resourceManagerAcl = resourceManagerAcl;
        this.nodeServiceAcl = nodeServiceAcl;
    }

    @Async("scaleExecutor")
    @Override
    public void scaleNode() {
        if(!SCALE_NODE_LOCK.tryAcquire()){
            return;
        }
        try {
            final int count = nodeRepository.countUsableNode(maxMemUsageRatio, maxCpuUsageRatio);
            if(count >= scaleCount){
                return;
            }
            for (int i = 0; i < scaleCount - count; i++) {
                // 获取新的节点来获取新的容器(本地数据存储和远程接口事物存在分布式事物,先不处理)
                final Optional<Node> nodeOpt = resourceManagerAcl.reserveNode(IdUtil.getRequestId(), SYS_ACCOUNT_ID);
                final Node node = nodeOpt.orElseThrow(() -> new NodeException("本地资源无法提供请求,且获取新node失败"));
                nodeRepository.save(node);
            }
        }catch (Exception e){
            log.warn("[scaleNode],发生异常,errMsg: {}", e.getMessage());
            if(log.isDebugEnabled()){
                log.debug("[scaleNode],发生异常,errMsg: {}", e.getMessage(), e);
            }
        }finally {
            SCALE_NODE_LOCK.release();
        }

    }

    @Override
    public void releaseNode() {
        final List<String> nodeIdList = nodeRepository.nodeIdList();
        if(CollectionUtils.isEmpty(nodeIdList)){
            return;
        }
        final int count = nodeRepository.countUsableNode(maxMemUsageRatio, maxCpuUsageRatio);
        if(count <= scaleCount){
            return;
        }
        for (String nodeId : nodeIdList) {
            final int containerCount = containerRepository.countByNodeId(nodeId);
            if(containerCount > 0){
                continue;
            }
            // 这里也需要分布式事物
            nodeRepository.remove(nodeId);
            resourceManagerAcl.releaseNode(IdUtil.getRequestId(), nodeId);
        }
    }

    @Override
    public void refresh() {
        final List<String> nodeIdList = nodeRepository.nodeIdList();

        final List<NodeStatVal> nodeStatValList = nodeIdList.stream()
                .map(nodeServiceAcl::asyncGetNodeStat)
                .map(future -> {
                    // 校验执行
                    try {
                        return future.get(1000, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        // 漏了就不做处理
                        log.warn("[refreshResourceStat],获取节点状态发生异常, errMsg: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(nodeStatValList)){
            return;
        }
        nodeStatValList.forEach(nodeStatVal -> {
            final Node node = Node.of(nodeStatVal.getNodeId(), nodeStatVal.getMemoryIdleSize(), nodeStatVal.getCpuUsageRatio());
            nodeRepository.update(node);
        });
    }

    @Override
    public Optional<Node> getAndUseNode(long memSize) {
        return nodeRepository.getAndUseNode(memSize, maxCpuRate);
    }

    @Override
    public void reserveAndSaveNode() {
        // 获取新的节点来获取新的容器(本地数据存储和远程接口事物存在分布式事物,先不处理)
        final Optional<Node> nodeOpt = resourceManagerAcl.reserveNode(IdUtil.getRequestId(), SYS_ACCOUNT_ID);
        final Node node = nodeOpt.orElseThrow(() -> new NodeException("本地资源无法提供请求,且获取新node失败"));
        nodeRepository.save(node);
    }
}
