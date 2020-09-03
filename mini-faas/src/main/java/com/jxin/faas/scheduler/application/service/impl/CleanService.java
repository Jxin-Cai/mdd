package com.jxin.faas.scheduler.application.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.jxin.faas.scheduler.application.acl.nodeservice.INodeServiceAcl;
import com.jxin.faas.scheduler.application.acl.resourcemanager.IResourceManagerAcl;
import com.jxin.faas.scheduler.application.service.ICleanService;
import com.jxin.faas.scheduler.application.service.IOneNodeCleanService;
import com.jxin.faas.scheduler.domain.entity.dmo.Node;
import com.jxin.faas.scheduler.domain.entity.val.NodeCleanVal;
import com.jxin.faas.scheduler.domain.service.IContainerManager;
import com.jxin.faas.scheduler.domain.service.INodeManager;
import com.jxin.faas.scheduler.domain.util.IJsonUtil;
import com.jxin.faas.scheduler.domain.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * 清理服务
 * @author Jxin
 * @version 1.0
 * @since 2020/7/29 15:29
 */
@Service
@Slf4j
public class CleanService implements ICleanService {
    private static final Semaphore CONTAINER_LOCK = new Semaphore(1);
    private static final Semaphore NODE_LOCK = new Semaphore(1);
    private final IOneNodeCleanService oneNodeCleanService;
    private final INodeManager nodeManager;
    private final IContainerManager containerManager;
    private final IResourceManagerAcl resourceManagerAcl;

    private final IJsonUtil jsonUtil;

    @Autowired
    public CleanService(IOneNodeCleanService oneNodeCleanService, INodeManager nodeManager, IJsonUtil jsonUtil, IContainerManager containerManager, IResourceManagerAcl resourceManagerAcl) {
        this.oneNodeCleanService = oneNodeCleanService;
        this.nodeManager = nodeManager;
        this.jsonUtil = jsonUtil;
        this.containerManager = containerManager;
        this.resourceManagerAcl = resourceManagerAcl;
    }
    @Override
    public void cleanNodeContainer() {
        if(!CONTAINER_LOCK.tryAcquire()){
            if(log.isDebugEnabled()){
                log.debug("[cleanNodeContainer],正在清理node上的容器,结束当前请求");
            }
            return;
        }
        try {
            final Set<Node> nodes = nodeManager.nodeSet();
            if(log.isDebugEnabled()){
                log.debug("[cleanNodeContainer],清理node开始,nodeSize: {},nodes: {}", nodes.size(), jsonUtil.beanJson(nodes));
            }
            nodes.forEach(oneNodeCleanService::cleanNodeContainer);
        }finally {
            CONTAINER_LOCK.release();
        }

    }

    @Override
    public void cleanIdleNode() {
        if(!NODE_LOCK.tryAcquire()){
            if(log.isDebugEnabled()){
                log.debug("[cleanNode],正在清理node,结束当前请求");
            }
            return;
        }
        try {
            while (true){
                final Set<Node> nodes = nodeManager.nodeSet();
                if(CollectionUtils.isEmpty(nodes)){
                    return;
                }

                final Node last = CollUtil.getLast(nodes);
                final NodeCleanVal nodeCleanVal = containerManager.cleanContainer(last.getId());
                if(nodeCleanVal.isNeedRemoveNode()){
                    if(log.isDebugEnabled()){
                        log.debug("[cleanNode],需要清理节点,nodeId: {}", last.getId());
                    }
                    nodeManager.removeNode(last.getId());
                    resourceManagerAcl.releaseNode(IdUtil.getRequestId(), last.getId());
                    continue;
                }

                if(CollectionUtils.isNotEmpty(nodeCleanVal.getRemoveContainerIdList())){
                    oneNodeCleanService.removeContainer(last, nodeCleanVal.getRemoveContainerIdList());
                }
                if(!nodeCleanVal.isNeedRemoveNode()){
                    return;
                }
            }
        }finally {
            NODE_LOCK.release();
        }

    }
}
