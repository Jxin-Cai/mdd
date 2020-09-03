package com.jxin.faas.scheduler.application.service.impl;

import com.google.common.collect.Maps;
import com.jxin.faas.scheduler.application.acl.nodeservice.INodeServiceAcl;
import com.jxin.faas.scheduler.application.acl.resourcemanager.IResourceManagerAcl;
import com.jxin.faas.scheduler.application.service.IScaleContainerService;
import com.jxin.faas.scheduler.domain.entity.dmo.Container;
import com.jxin.faas.scheduler.domain.entity.dmo.Node;
import com.jxin.faas.scheduler.domain.entity.val.FunctionInfoVal;
import com.jxin.faas.scheduler.domain.service.IContainerManager;
import com.jxin.faas.scheduler.domain.service.INodeManager;
import com.jxin.faas.scheduler.domain.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;

/**
 * 扩容容器的服务
 * @author Jxin
 * @version 1.0
 * @since 2020/7/28 19:50
 */
@Service
@Slf4j
public class AsyncScaleContainerService implements IScaleContainerService {
    private static final Map<String, Semaphore> FUNC_NAME_LOCK = Maps.newConcurrentMap();

    private final INodeManager nodeManager;
    private final IContainerManager containerManager;
    private final INodeServiceAcl nodeServiceAcl;
    private final IResourceManagerAcl resourceManagerAcl;

    @Autowired
    public AsyncScaleContainerService(INodeManager nodeManager,
                                      IContainerManager containerManager,
                                      INodeServiceAcl nodeServiceAcl,
                                      IResourceManagerAcl resourceManagerAcl) {
        this.nodeManager = nodeManager;
        this.containerManager = containerManager;
        this.nodeServiceAcl = nodeServiceAcl;
        this.resourceManagerAcl = resourceManagerAcl;
    }

    @Async("scaleExecutor")
    @Override
    public void scaleContainer(FunctionInfoVal functionInfoVal, int count) {

        final Semaphore semaphore =
                FUNC_NAME_LOCK.computeIfAbsent(functionInfoVal.getName(), s -> new Semaphore(1));
        if(!semaphore.tryAcquire()){
            if(log.isDebugEnabled()){
                log.debug("[scaleContainer],该函数正在执行扩容,跳过本次扩容请求,funcName: {}",
                          functionInfoVal.getName());
            }
            return;
        }
        if(log.isDebugEnabled()){
            log.debug("[scaleContainer],函数执行扩容,funcName: {}, count: {}",
                    functionInfoVal.getName(), count);
        }
        try {
            for (int i = 0; i < count; i++) {
                scaleOneContainer(functionInfoVal);
            }
        }finally {
            semaphore.release();
        }
    }

    /**
     * 扩容一个容器
     * @param functionInfoVal 函数信息值对象
     */
    private void scaleOneContainer(FunctionInfoVal functionInfoVal) {
        final Optional<Node> nodeOptional = nodeManager.getNode(functionInfoVal.getMemorySize());

        if(nodeOptional.isPresent()){
            final Node node = nodeOptional.get();
            try {
                final Optional<Container> containerOptional =
                        nodeServiceAcl.loopGetContainer(IdUtil.getRequestId(), functionInfoVal, node, 1);
                if(!containerOptional.isPresent()){
                    node.setEnough(false);
                    return;
                }
                final Container container = containerOptional.get();
                containerManager.addContainer(container);
                return;
            }finally {
                node.release();
            }

        }

        final Optional<Node> nodeOpt = resourceManagerAcl.reserveNode(IdUtil.getRequestId(), "s");
        if(!nodeOpt.isPresent()){
            // scaleOneContainer(functionInfoVal);
            return;
        }
        final Node node = nodeOpt.get();
        final Optional<Container> containerOptional =
                nodeServiceAcl.loopGetContainer(IdUtil.getRequestId(), functionInfoVal, node, Integer.MAX_VALUE);
        final Container container = containerOptional.get();
        containerManager.addContainer(container);
        nodeManager.addNode(node);
    }
}
