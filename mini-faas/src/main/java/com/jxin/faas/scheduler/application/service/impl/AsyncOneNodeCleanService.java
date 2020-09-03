package com.jxin.faas.scheduler.application.service.impl;

import com.jxin.faas.scheduler.application.acl.nodeservice.INodeServiceAcl;
import com.jxin.faas.scheduler.application.service.IOneNodeCleanService;
import com.jxin.faas.scheduler.domain.entity.dmo.Node;
import com.jxin.faas.scheduler.domain.entity.val.NodeCleanVal;
import com.jxin.faas.scheduler.domain.service.IContainerManager;
import com.jxin.faas.scheduler.domain.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * 一个节点的清理服务
 * @author Jxin
 * @version 1.0
 * @since 2020/7/28 20:29
 */
@Slf4j
@Service
public class AsyncOneNodeCleanService implements IOneNodeCleanService {
    private static final Map<String, Semaphore> NODE_ID_LOCK = new ConcurrentHashMap<>(20);


    private final IContainerManager containerManager;
    private final INodeServiceAcl nodeServiceAcl;

    @Autowired
    public AsyncOneNodeCleanService(IContainerManager containerManager,
                                    INodeServiceAcl nodeServiceAcl) {
        this.containerManager = containerManager;
        this.nodeServiceAcl = nodeServiceAcl;
    }

    @Async("cleanExecutor")
    @Override
    public void cleanNodeContainer(Node node) {
        final Semaphore semaphore =
                NODE_ID_LOCK.computeIfAbsent(node.getId(), s -> new Semaphore(1));
        if(!semaphore.tryAcquire()){
            if(log.isDebugEnabled()){
                log.debug("[cleanNodeContainer],当前节点正在执行清理,跳过本次清理请求,nodeId: {}", node.getId());
            }
            return;
        }
        try {
            final NodeCleanVal nodeCleanVal = containerManager.cleanContainer(node.getId());
            final List<String> removeContainerIdList = nodeCleanVal.getRemoveContainerIdList();
            if(CollectionUtils.isEmpty(removeContainerIdList)){
                return;
            }
            removeContainer(node, removeContainerIdList);
        }finally {
            semaphore.release();
        }

    }

    /**
     * 删除节点上的容器
     * @param  node                  节点
     * @param  removeContainerIdList 要删除的容器列表
     */
    public void removeContainer(Node node, List<String> removeContainerIdList) {
        final Instant start = Instant.now();
        // 删除容器计数器
        final CountDownLatch latch = new CountDownLatch(removeContainerIdList.size());
        removeContainerIdList
                .forEach(containerId -> nodeServiceAcl.removeContainer(IdUtil.getRequestId(), node.getId(), containerId, latch));
        try {
            latch.await();
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        if(log.isDebugEnabled()){
            log.debug("[cleanNodeContainer],清理节点上的函数,耗时: {} ms, removeContainerSize: {}",
                    ChronoUnit.MILLIS.between(start, Instant.now()),removeContainerIdList.size());
        }
    }
}
