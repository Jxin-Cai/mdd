package com.jxin.faas.scheduler.application.service.impl;

import com.jxin.faas.scheduler.application.service.IRefreshOneNodeService;
import com.jxin.faas.scheduler.domain.entity.dmo.Node;
import com.jxin.faas.scheduler.domain.entity.val.NodeStatVal;
import com.jxin.faas.scheduler.domain.service.IContainerManager;
import com.jxin.faas.scheduler.domain.service.INodeManager;
import com.jxin.faas.scheduler.domain.util.IJsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * 异步刷新单节点
 * @author Jxin
 * @version 1.0
 * @since 2020/7/28 17:54
 */
@Service
@Slf4j
public class AsyncRefreshOneNodeService implements IRefreshOneNodeService {
    private static final Map<String, Semaphore> NODE_ID_LOCK = new ConcurrentHashMap<>(20);


    private final INodeManager nodeManager;
    private final IContainerManager containerManager;
    private final IJsonUtil jsonUtil;
    @Autowired
    public AsyncRefreshOneNodeService(INodeManager nodeManager, IContainerManager containerManager, IJsonUtil jsonUtil) {
        this.nodeManager = nodeManager;
        this.containerManager = containerManager;
        this.jsonUtil = jsonUtil;
    }


    @Async("refreshExecutor")
    @Override
    public void refreshNodeStatAsync(NodeStatVal nodeStatVal, CountDownLatch countDownLatch) {
        if(null == nodeStatVal){
            return;
        }
        try {
            refreshNodeStat(nodeStatVal);
        }finally {
            countDownLatch.countDown();
        }
    }

    @Override
    public void refreshNodeStat(NodeStatVal nodeStatVal) {
        if(null == nodeStatVal){
            return;
        }
        final Semaphore semaphore =
                NODE_ID_LOCK.computeIfAbsent(nodeStatVal.getNodeId(), s -> new Semaphore(1));
        if(!semaphore.tryAcquire()){
            if(log.isDebugEnabled()){
                log.debug("[refreshNodeStatAsync],当前节点正在刷新状态,跳过本次刷新请求,nodeId: {}",
                        nodeStatVal.getNodeId());
            }
            return;
        }
        try {
            nodeManager.refreshNodeStat(nodeStatVal);
            final Node node = nodeManager.getNodeById(nodeStatVal.getNodeId());
            containerManager.refreshContainerStat(nodeStatVal.getNodeId(), !node.isEnough(), nodeStatVal.getContainerStatList());
        }finally {
            semaphore.release();
        }
    }

}
