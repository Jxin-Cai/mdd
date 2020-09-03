package com.jxin.faas.scheduler.application.service.impl;

import com.google.common.collect.Maps;
import com.jxin.faas.scheduler.application.acl.resourcemanager.IResourceManagerAcl;
import com.jxin.faas.scheduler.application.service.IScaleContainerService;
import com.jxin.faas.scheduler.application.service.IScaleService;
import com.jxin.faas.scheduler.domain.entity.dmo.Function;
import com.jxin.faas.scheduler.domain.entity.dmo.Node;
import com.jxin.faas.scheduler.domain.service.IContainerManager;
import com.jxin.faas.scheduler.domain.service.IFunctionManager;
import com.jxin.faas.scheduler.domain.service.INodeManager;
import com.jxin.faas.scheduler.domain.util.IJsonUtil;
import com.jxin.faas.scheduler.domain.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * 扩容服务
 * @author Jxin
 * @version 1.0
 * @since 2020/7/28 19:29
 */
@Service
@Slf4j
public class ScaleService implements IScaleService {
    private static final Semaphore SCALE_NODE_LOCK = new Semaphore(1);

    @Value("${scaleService.maxIntervalTime}")
    private Integer maxIntervalTime;
    @Value("${scaleService.reserveNodeTtl}")
    private Integer reserveNodeTtl;

    private final INodeManager nodeManager;
    private final IContainerManager containerManager;
    private final IFunctionManager functionManager;


    private final IResourceManagerAcl resourceManagerAcl;
    private final IScaleContainerService scaleContainerService;
    private final IJsonUtil jsonUtil;

    @Autowired
    public ScaleService(INodeManager nodeManager,
                        IContainerManager containerManager,
                        IFunctionManager functionManager,
                        IResourceManagerAcl resourceManagerAcl,
                        IScaleContainerService scaleContainerService,
                        IJsonUtil jsonUtil) {
        this.nodeManager = nodeManager;
        this.containerManager = containerManager;
        this.functionManager = functionManager;
        this.resourceManagerAcl = resourceManagerAcl;
        this.scaleContainerService = scaleContainerService;
        this.jsonUtil = jsonUtil;
    }

    @Async("scaleExecutor")
    @Override
    public void scaleNode() {
        if(!SCALE_NODE_LOCK.tryAcquire()){
            return;
        }

        try {
            if(!nodeManager.needScaleNode()){
                if(log.isDebugEnabled()){
                    log.debug("[scaleNode],定时扩容,无需扩容node");
                }
                return;
            }
            if(nodeManager.nodesIsMaxSize()){
                if(log.isDebugEnabled()){
                    log.debug("[scaleNode],node数已满,无需扩容,无需扩容node");
                }
                return;
            }

            final Optional<Node> nodeOpt = loopGetNode(reserveNodeTtl);
            if(!nodeOpt.isPresent()){
                if(log.isDebugEnabled()){
                    log.debug("[scaleNode],定时扩容,申请node超过{},放弃当前扩容", reserveNodeTtl);
                }
                return;
            }
            final Node node = nodeOpt.get();
            nodeManager.addNode(node);
            if(log.isDebugEnabled()){
                log.debug("[scaleNode],定时扩容,创建新容器,node: {}", jsonUtil.beanJson(node));
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
    public void scaleFuncContainer() {
        final Set<String> allFuncName = containerManager.getAllFuncName();
        if(log.isDebugEnabled()){
            log.debug("[scaleFuncContainer],函数扩容,检查的函数数量 : {}", allFuncName.size());
        }
        final Map<String, Integer> needScaleFuncMap = Maps.newHashMap();
        for (String funcName : allFuncName) {
            final int count = containerManager.countScaleFuncContainer(funcName);
            if(count == 0){
                continue;
            }
            needScaleFuncMap.put(funcName, count);
        }
        if(log.isDebugEnabled()){
            log.debug("[scaleFuncContainer],函数扩容,需要扩容的函数列表 : {}", jsonUtil.beanJson(needScaleFuncMap));
        }
        needScaleFuncMap.forEach((k, v) ->
            functionManager.getFunction(k).ifPresent(function -> scaleOneFuncContainer(function, v))
        );
    }
    //*******************************************scaleFuncContainer*****************************************************
    /**
     * 扩大一个函数的容器数量
     * @param function 函数
     * @param count    数量
     */
    private void scaleOneFuncContainer(Function function, int count){
        final boolean need = function.needScale(maxIntervalTime);
        if(need){
            scaleContainerService.scaleContainer(function.getFunctionInfoVal(), count);
        }
    }
    //*******************************************common*****************************************************************
    /**
     * 循环创建节点
     * @param ttl 超时时间
     * @return 容器
     */
    private Optional<Node> loopGetNode(int ttl) {
        final Instant start = Instant.now();
        while (true){
            if(ChronoUnit.SECONDS.between(start, Instant.now()) > ttl){
                return Optional.empty();
            }
            final Optional<Node> nodeOpt = resourceManagerAcl.reserveNode(IdUtil.getRequestId(), "s");
            if(!nodeOpt.isPresent()){
                continue;
            }
            return nodeOpt;
        }
    }
}
