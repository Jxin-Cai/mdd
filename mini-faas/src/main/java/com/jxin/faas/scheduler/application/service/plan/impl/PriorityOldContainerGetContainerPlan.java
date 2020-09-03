package com.jxin.faas.scheduler.application.service.plan.impl;

import com.google.common.collect.Maps;
import com.jxin.faas.scheduler.application.acl.nodeservice.INodeServiceAcl;
import com.jxin.faas.scheduler.application.service.IScaleContainerService;
import com.jxin.faas.scheduler.application.service.plan.IReuseResourceGetContainerPlan;
import com.jxin.faas.scheduler.domain.entity.dmo.Container;
import com.jxin.faas.scheduler.domain.entity.dmo.Node;
import com.jxin.faas.scheduler.domain.entity.val.FunctionInfoVal;
import com.jxin.faas.scheduler.domain.service.IContainerManager;
import com.jxin.faas.scheduler.domain.service.INodeManager;
import com.jxin.faas.scheduler.domain.util.IJsonUtil;
import com.jxin.faas.scheduler.domain.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;

/**
 *
 * 优先老容器 容器的策略
 * @author Jxin
 * @version 1.0
 * @since 2020/8/1 11:31 下午
 */
@Service
@Slf4j
public class PriorityOldContainerGetContainerPlan implements IReuseResourceGetContainerPlan {
    private static final Map<String, Object> FUNC_NAME_LOCK = Maps.newConcurrentMap();

    private final INodeManager nodeManager;
    private final IContainerManager containerManager;
    private final INodeServiceAcl nodeServiceAcl;
    private final IJsonUtil jsonUtil;
    private final IScaleContainerService scaleContainerService;

    public PriorityOldContainerGetContainerPlan(INodeManager nodeManager, IContainerManager containerManager, INodeServiceAcl nodeServiceAcl, IJsonUtil jsonUtil, IScaleContainerService scaleContainerService) {
        this.nodeManager = nodeManager;
        this.containerManager = containerManager;
        this.nodeServiceAcl = nodeServiceAcl;
        this.jsonUtil = jsonUtil;
        this.scaleContainerService = scaleContainerService;
    }

    @Override
    public Optional<Container> reuseResourceGetContainer(String requestId, FunctionInfoVal functionInfoVal) {
        // cass1
        final Optional<Container> containerOptional = containerManager.getAndUseContainer(requestId, functionInfoVal);
        if(containerOptional.isPresent()){
            if(log.isDebugEnabled()){
                log.debug("[getContainer],cass1,复用已有容器");
            }
            return containerOptional;
        }
        final Object lock =
                FUNC_NAME_LOCK.computeIfAbsent(functionInfoVal.getName(), s -> new Object());
        // case2
        // 发起异步扩容申请
        synchronized (lock){
            final Optional<Container> containerOpti = containerManager.getAndUseContainer(requestId, functionInfoVal);
            if(containerOpti.isPresent()){
                if(log.isDebugEnabled()){
                    log.debug("[getContainer],cass1,复用已有容器");
                }
                return containerOpti;
            }
            // 异步扩容
            final int count = containerManager.countScaleFuncContainer(functionInfoVal.getName());
            scaleContainerService.scaleContainer(functionInfoVal, count);

            final Optional<Container> containerOpt = nodeManager.getContainer(functionInfoVal, this::getNewContainer);
            if(!containerOpt.isPresent()){
                return containerOpt;
            }
            final Container container = containerOpt.get();
            container.addRunJob(requestId);
            containerManager.addContainer(container);
            if(log.isDebugEnabled()){
                log.debug("[getContainer],cass2,复用已有node,创建新容器,container: {}", jsonUtil.beanJson(container));
            }
            return Optional.of(container);
        }
    }

    /**
     * 用新的节点 创建新容器返回
     * @param  functionInfoVal 函数信息值对象
     * @return 容器
     */
    private Optional<Container> getNewContainer(FunctionInfoVal functionInfoVal) {
        // 新建容器承接任务
        final Optional<Node> nodeOptional = nodeManager.getNode(functionInfoVal.getMemorySize());
        if(!nodeOptional.isPresent()){
            return Optional.empty();
        }

        final Node node = nodeOptional.get();
        try {
            final Optional<Container> containerOpt =
                    nodeServiceAcl.loopGetContainer(IdUtil.getRequestId(), functionInfoVal, node, 1);
            if(!containerOpt.isPresent()){
                if(log.isDebugEnabled()){
                    log.debug("[getContainer],cass2,node标记为失败: nodeOrder{}", node.getOrder());
                }
                node.setEnough(false);
                return getNewContainer(functionInfoVal);
            }
            return containerOpt;
        }finally {
            node.release();
        }
    }

    /**
     * 线程等待
     * @param  awaitTime 等待时间
     */
    private void await(int awaitTime) {
        try {
            Thread.sleep(awaitTime);
        }catch (Exception e){
            log.warn(e.getMessage(), e);
        }
    }
}
