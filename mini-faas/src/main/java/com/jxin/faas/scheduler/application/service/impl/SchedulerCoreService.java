package com.jxin.faas.scheduler.application.service.impl;

import cn.hutool.core.util.StrUtil;
import com.jxin.faas.scheduler.application.acl.nodeservice.INodeServiceAcl;
import com.jxin.faas.scheduler.application.acl.resourcemanager.IResourceManagerAcl;
import com.jxin.faas.scheduler.application.service.ICleanService;
import com.jxin.faas.scheduler.application.service.IRefreshResourceService;
import com.jxin.faas.scheduler.application.service.IScaleService;
import com.jxin.faas.scheduler.application.service.ISchedulerCoreService;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * 调度器核心服务
 * @author Jxin
 * @version 1.0
 * @since 2020/7/21 16:41
 */
@Service
@Slf4j
public class SchedulerCoreService implements ISchedulerCoreService {
    private static final Object LOCK = new Object();
    @Value("${schedulerCore.initSize}")
    private Integer initSize;
    @Value("${schedulerCore.awaitTime}")
    private Integer awaitTime;
    @Value("${schedulerCore.enableGetContainerPlan:priorityOldContainerGetContainerPlan}")
    private String enableGetContainerPlan;

    private final INodeManager nodeManager;
    private final IContainerManager containerManager;
    private final IFunctionManager functionManager;
    private final INodeServiceAcl nodeServiceAcl;
    private final IResourceManagerAcl resourceManagerAcl;
    private final IJsonUtil jsonUtil;
    private final IScaleService scaleService;
    private final ICleanService cleanService;
    private final Map<String, IReuseResourceGetContainerPlan> reuseResourceGetContainerPlanMap;
    private final IRefreshResourceService refreshResourceService;
    @Autowired
    public SchedulerCoreService(INodeManager nodeManager,
                                IContainerManager containerManager,
                                IFunctionManager functionManager,
                                INodeServiceAcl nodeServiceAcl,
                                IResourceManagerAcl resourceManagerAcl,
                                IJsonUtil jsonUtil,
                                IScaleService scaleService,
                                ICleanService cleanService,
                                Map<String, IReuseResourceGetContainerPlan> reuseResourceGetContainerPlanMap, IRefreshResourceService refreshResourceService) {
        this.nodeManager = nodeManager;
        this.containerManager = containerManager;
        this.functionManager = functionManager;
        this.nodeServiceAcl = nodeServiceAcl;
        this.resourceManagerAcl = resourceManagerAcl;
        this.jsonUtil = jsonUtil;
        this.scaleService = scaleService;
        this.cleanService = cleanService;
        this.reuseResourceGetContainerPlanMap = reuseResourceGetContainerPlanMap;
        this.refreshResourceService = refreshResourceService;
    }


    @Override
    public void initNodes() {
        if(log.isDebugEnabled()){
            log.debug("[initNodes],初始化容器,初始数量: {}", initSize);
        }
        try {
            for (int i = 0; i < initSize; i++) {
                final Optional<Node> nodeOptional = resourceManagerAcl.reserveNode(IdUtil.getRequestId(),i + "");
                if(!nodeOptional.isPresent()){
                    continue;
                }
                final Node node = nodeOptional.get();
                nodeManager.addNode(node);
            }
        }catch (Exception e){
            log.warn(e.getMessage(), e);
        }
    }

    /**
     * 获取容器
     * @param  requestId       请求Id
     * @param  accountId       用户id
     * @param  functionInfoVal 函数信息值对象
     * @return 容器
     */
    @Override
    public Container getContainer(String requestId, String accountId, FunctionInfoVal functionInfoVal) {
        // 记录函数使用
        functionManager.pushFunc(functionInfoVal);
/*        if(!success && functionInfoVal.getMemorySize() != 0){
            if(log.isDebugEnabled()){
                log.debug("[getContainer],并发量过大的函数请求跳过,函数名: {}", functionInfoVal.getName());
            }
            return null;
        }*/
/*        if(functionManager.isBadFunc(functionInfoVal.getName())){
            if(log.isDebugEnabled()){
                log.debug("[getContainer],过滤异常函数,函数名: {}", functionInfoVal.getName());
            }
            return null;
        }*/
        return doGetContainer(requestId, accountId, functionInfoVal);
    }

    /**
     * 完结运行中的任务
     * @param  requestId   请求Id
     * @param  containerId 容器id
     * @param  success     函数是否执行成功
     */
    @Override
    public void finishRunJob(String requestId, String containerId, boolean success) {
        containerManager.reduceContainerJob(requestId, containerId, success);
    }



    //*******************************************getContainer**********************************************************

    /**
     * 获取容器
     * cass1,复用已有容器
     * cass2,复用已有node,创建新容器
     * cass3,创建新的node,创建新容器
     * @param  requestId       请求Id
     * @param  accountId       用户id
     * @param  functionInfoVal 函数信息值对象
     * @return 容器
     */
    private Container doGetContainer(String requestId, String accountId, FunctionInfoVal functionInfoVal) {
        final IReuseResourceGetContainerPlan reuseResourceGetContainerPlan =
                reuseResourceGetContainerPlanMap.get(enableGetContainerPlan);
        // cass1 cass2
        final Optional<Container> containerOptional =
                reuseResourceGetContainerPlan.reuseResourceGetContainer(requestId, functionInfoVal);
        if(containerOptional.isPresent()){
            return containerOptional.get();
        }

        refreshResourceService.refreshResourceStat();
        // case3 并发情况仅允许一个节点创建node
        synchronized (LOCK){
            final Optional<Container> containerOpt =
                    reuseResourceGetContainerPlan.reuseResourceGetContainer(requestId, functionInfoVal);
            if(containerOpt.isPresent()){
                return containerOpt.get();
            }

            // 已有资源无法支撑就触发资源清理
            cleanService.cleanNodeContainer();

/*            if(nodeManager.nodesIsMaxSize()){
                refreshResourceService.refreshResourceStat();
                return doGetContainer(requestId, accountId, functionInfoVal);
            }*/

            final Optional<Node> nodeOpt = resourceManagerAcl.reserveNode(IdUtil.getRequestId(), accountId);
            if(!nodeOpt.isPresent()){
                scaleService.scaleNode();
                throw new RuntimeException("[无法创建新节点]");
            }
            final Node node = nodeOpt.get();
            final Container result = nodeServiceAcl.loopGetContainer(IdUtil.getRequestId(),
                                                                     functionInfoVal,
                                                                     node,
                                                                     10)
                                                   .orElseThrow(() -> {
                                                       nodeManager.addNode(node);
                                                       if(log.isDebugEnabled()){
                                                           log.debug("[getContainer],cass3,新节点无法创建函数,入参: {}",
                                                                     jsonUtil.beanJson(functionInfoVal));
                                                       }
                                                       functionManager.recordErrFunc(functionInfoVal.getName());
                                                       return new RuntimeException(
                                                               StrUtil.format("[新节点无法创建函数],入参: {}",
                                                                       jsonUtil.beanJson(functionInfoVal)));
                                                   });
            containerManager.addContainer(result);
            result.addRunJob(requestId);
            containerManager.addContainer(result);
            nodeManager.addNode(node);
            if(log.isDebugEnabled()){
                log.debug("[getContainer],cass3,创建新的node,创建新容器,nodeSize: {}, node: {}",
                          nodeManager.nodeSize(), jsonUtil.beanJson(node));
            }
            return result;
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
