package com.jxin.faas.scheduler.application.service.impl;

import com.jxin.faas.scheduler.application.acl.nodeservice.INodeServiceAcl;
import com.jxin.faas.scheduler.application.service.IRefreshOneNodeService;
import com.jxin.faas.scheduler.application.service.IRefreshResourceService;
import com.jxin.faas.scheduler.domain.entity.dmo.Node;
import com.jxin.faas.scheduler.domain.entity.val.NodeStatVal;
import com.jxin.faas.scheduler.domain.service.INodeManager;
import com.jxin.faas.scheduler.domain.util.IJsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Jxin
 * @version 1.0
 * @since 2020/8/8 10:33
 */
@Service
@Slf4j
public class RefreshResourceService implements IRefreshResourceService {
    private static final Semaphore NODE_LOCK = new Semaphore(1);
    private static final Map<String, Semaphore> NODE_ID_LOCK = new ConcurrentHashMap<>(20);

    private final INodeManager nodeManager;
    private final INodeServiceAcl nodeServiceAcl;
    private final IRefreshOneNodeService refreshOneNodeService;
    private final IJsonUtil jsonUtil;

    @Autowired
    public RefreshResourceService(INodeManager nodeManager, INodeServiceAcl nodeServiceAcl, IRefreshOneNodeService refreshOneNodeService, IJsonUtil jsonUtil) {
        this.nodeManager = nodeManager;
        this.nodeServiceAcl = nodeServiceAcl;
        this.refreshOneNodeService = refreshOneNodeService;
        this.jsonUtil = jsonUtil;
    }

    /**
     * 刷新资源状态
     */
    @Override
    public void refreshResourceStat() {
        if(!NODE_LOCK.tryAcquire()){
            if(log.isDebugEnabled()){
                log.debug("[refreshResourceStat],正在进行资源刷新,跳出逻辑");
            }
            return;
        }
        try {
            final Set<Node> nodes = nodeManager.nodeSet();
            if(CollectionUtils.isEmpty(nodes)){
                return;
            }
            final Instant start = Instant.now();
            final Map<String, NodeStatVal> nodeStatMap =
                    nodes.stream()
                            .map(node -> nodeServiceAcl.asyncGetNodeStat(node.getId()))
                            .map(future -> {
                                // 校验执行
                                try {
                                    return future.get(1000, TimeUnit.SECONDS);
                                } catch (Exception e) {
                                    log.warn("[refreshResourceStat],获取节点状态发生异常, errMsg: {}", e.getMessage());
                                    throw new IllegalArgumentException("[refreshResourceStat],获取节点状态发生异常");
                                }
                            })
                            .collect(Collectors.toMap(NodeStatVal::getNodeId, nodeStatVal -> nodeStatVal));
            // 刷新节点以及下边容器的状态
            final CountDownLatch latch = new CountDownLatch(nodes.size());
            nodes.forEach(node -> refreshOneNodeService.refreshNodeStatAsync(nodeStatMap.get(node.getId()), latch));

            latch.await();
            if (log.isDebugEnabled()) {
                log.debug("[refreshResourceStat],当前节点状态,耗时: {} ms,nodes: {}", ChronoUnit.MILLIS.between(start, Instant.now()), jsonUtil.beanJson(nodes));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            NODE_LOCK.release();
        }
    }

    @Override
    public void refreshOneNodeStat(String nodeId) {
        final Semaphore semaphore =
                NODE_ID_LOCK.computeIfAbsent(nodeId, s -> new Semaphore(1));
        if(!semaphore.tryAcquire()){
            if(log.isDebugEnabled()){
                log.debug("[refreshOneNodeStat],当前节点正在刷新状态,跳过本次刷新请求,nodeId: {}", nodeId);
            }
            return;
        }
        try {
            final Node node = nodeManager.getNodeById(nodeId);
            if(null == node){
                return;
            }
            final NodeStatVal nodeStat;
            try {
                nodeStat = nodeServiceAcl.getNodeStat(node.getId());
            }catch (Exception e){
                return;
            }
            refreshOneNodeService.refreshNodeStat(nodeStat);
        }finally {
            semaphore.release();
        }

    }
}
