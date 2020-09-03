package com.jxin.faas.scheduler.application.service.plan.impl;

import com.google.common.collect.Maps;
import com.jxin.faas.scheduler.application.acl.nodeservice.INodeServiceAcl;
import com.jxin.faas.scheduler.application.service.IRefreshResourceService;
import com.jxin.faas.scheduler.application.service.plan.IReuseResourceGetContainerPlan;
import com.jxin.faas.scheduler.domain.entity.dmo.Container;
import com.jxin.faas.scheduler.domain.entity.dmo.Node;
import com.jxin.faas.scheduler.domain.entity.val.FunctionInfoVal;
import com.jxin.faas.scheduler.domain.service.IContainerManager;
import com.jxin.faas.scheduler.domain.service.IFunctionManager;
import com.jxin.faas.scheduler.domain.service.INodeManager;
import com.jxin.faas.scheduler.domain.util.IJsonUtil;
import com.jxin.faas.scheduler.domain.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 *
 * 遍历节点获取 容器的策略
 * @author Jxin
 * @version 1.0
 * @since 2020/8/1 11:31 下午
 */
@Service
@Slf4j
public class LoopNodeGetContainerPlan implements IReuseResourceGetContainerPlan {
    private static final Map<String, Object> FUNC_NAME_LOCK = Maps.newConcurrentMap();
    private final INodeManager nodeManager;
    private final IContainerManager containerManager;
    private final IFunctionManager functionManager;
    private final INodeServiceAcl nodeServiceAcl;
    private final IJsonUtil jsonUtil;
    private final IRefreshResourceService refreshResourceService;
    @Autowired
    public LoopNodeGetContainerPlan(INodeManager nodeManager, IContainerManager containerManager, INodeServiceAcl nodeServiceAcl, IJsonUtil jsonUtil, IFunctionManager functionManager, IRefreshResourceService refreshResourceService) {
        this.nodeManager = nodeManager;
        this.containerManager = containerManager;
        this.nodeServiceAcl = nodeServiceAcl;
        this.jsonUtil = jsonUtil;
        this.functionManager = functionManager;
        this.refreshResourceService = refreshResourceService;
    }

    @Override
    public Optional<Container> reuseResourceGetContainer(String requestId, FunctionInfoVal functionInfoVal) {
        return nodeManager.getContainer(functionInfoVal, functionInfo -> loopNodeGetContainer(requestId, functionInfo));
    }
    /**
     * 循环node获取函数容器
     * @param  functionInfoVal 函数信息值对象
     * @return 容器
     */
    private Optional<Container> loopNodeGetContainer(String requestId, FunctionInfoVal functionInfoVal) {
        final Set<Node> nodes = nodeManager.nodeSet();
        for (Node node : nodes) {
            // cass1
            refreshResourceService.refreshOneNodeStat(node.getId());
            final Optional<Container> containerOptional =
                    containerManager.getAndUseContainerByNode(requestId, node.getId(), functionInfoVal);
            if(containerOptional.isPresent()){
                if(log.isDebugEnabled()){
                    log.debug("[getContainer],cass1,复用已有容器");
                }
                return containerOptional;
            }
            final Object lock =
                    FUNC_NAME_LOCK.computeIfAbsent(functionInfoVal.getName(), s -> new Object());

            synchronized (lock){
                // cass1
                refreshResourceService.refreshOneNodeStat(node.getId());
                final Optional<Container> containerOpt1 =
                        containerManager.getAndUseContainerByNode(requestId, node.getId(), functionInfoVal);
                if(containerOpt1.isPresent()){
                    if(log.isDebugEnabled()){
                        log.debug("[getContainer],cass1,复用已有容器");
                    }
                    return containerOptional;
                }

                // cass2
                if (!node.reduceMem(functionInfoVal.getMemorySize())){
                    continue;
                }
                try {
                    final Optional<Container> containerOpt =
                            nodeServiceAcl.loopGetContainer(IdUtil.getRequestId(), functionInfoVal, node, 1);
                    if(!containerOpt.isPresent()){
                        if(log.isDebugEnabled()) {
                            log.debug("[getContainer],cass2,扣减内存成功但无法创建新的容器,创建新容器,node: {}, func: {}",
                                      jsonUtil.beanJson(node), jsonUtil.beanJson(functionInfoVal));
                        }
                        functionManager.recordErrFunc(functionInfoVal.getName());
                        continue;
                        // throw new RuntimeException("内存扣减成功节点无法创建函数");
                    }
                    final Container container = containerOpt.get();
                    container.addRunJob(requestId);
                    containerManager.addContainer(container);
                    if(log.isDebugEnabled()){
                        log.debug("[getContainer],cass2,复用已有node");
                    }
                    return Optional.of(container);
                }finally {
                    node.release();
                }

            }

        }
        return Optional.empty();
    }
}
