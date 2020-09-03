package com.jxin.faas.scheduler.domain.service.impl;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.util.NumberUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jxin.faas.scheduler.domain.entity.dmo.Container;
import com.jxin.faas.scheduler.domain.entity.val.ContainerStatVal;
import com.jxin.faas.scheduler.domain.entity.val.FunctionInfoVal;
import com.jxin.faas.scheduler.domain.entity.val.NodeCleanVal;
import com.jxin.faas.scheduler.domain.service.IContainerManager;
import com.jxin.faas.scheduler.domain.util.IJsonUtil;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 容器管理器
 * @author Jxin
 * @version 1.0
 * @since 2020/7/28 14:17
 */
@Service
@Slf4j
public class ContainerManager implements IContainerManager {
    //**********************************************静态变量************************************************************
    /**票据锁*/
    private static final StampedLock STAMPED_LOCK = new StampedLock();
    /**
     * funcName : Container
     */
    private static final Map<String, Set<Container>> FUNC_CONTAINER_MAP = Maps.newConcurrentMap();
    /**
     * containerId : Container
     */
    private static final Map<String, Container> CONTAINER_MAP = Maps.newConcurrentMap();
    /**
     * 节点Id : Container
     */
    private static final Map<String, Set<Container>> NODE_CONTAINER_MAP = Maps.newConcurrentMap();
    /**
     * 节点Id : <funcName : Container>
     */
    private static final Map<String, Map<String, Set<Container>>> NODE_FUNC_CONTAINER_MAP = Maps.newConcurrentMap();
    //**********************************************配置变量************************************************************
    @Value("${containerManager.maxCpuUsageRatio}")
    private BigDecimal maxCpuUsageRatio;
    @Value("${containerManager.maxMemUsageRatio}")
    private BigDecimal maxMemUsageRatio;
    @Value("${containerManager.needScaleCpuUsageRatio}")
    private BigDecimal needScaleCpuUsageRatio;
    @Value("${containerManager.needScaleMemUsageRatio}")
    private BigDecimal needScaleMemUsageRatio;

    @Value("${containerManager.maxDelayCleanTime}")
    private Integer maxDelayCleanTime;
    @Value("${containerManager.expandDivisor}")
    private BigDecimal expandDivisor;
    //**********************************************工具****************************************************************
    private final IJsonUtil jsonUtil;

    @Autowired
    public ContainerManager(IJsonUtil jsonUtil) {
        this.jsonUtil = jsonUtil;

    }

    @Override
    public Optional<Container> getAndUseContainerByNode(String requestId, String nodeId, FunctionInfoVal functionInfoVal) {

        final Map<String, Set<Container>> funcContainerMap = NODE_FUNC_CONTAINER_MAP.get(nodeId);
        if(MapUtils.isEmpty(funcContainerMap)){
            return Optional.empty();
        }
        return readLock(functionInfoVal, functionInfo ->
                doGetAndUseContainer(requestId, functionInfo, funcContainerMap.get(functionInfo.getName())));
    }

    /**
     * 获取并使用容器
     * @param  requestId       请求Id
     * @param  functionInfoVal 函数信息值对象
     * @return 容器领域对象
     */
    @Override
    public Optional<Container> getAndUseContainer(String requestId, FunctionInfoVal functionInfoVal) {
        return readLock(functionInfoVal, functionInfo ->
                doGetAndUseContainer(requestId, functionInfo, FUNC_CONTAINER_MAP.get(functionInfo.getName())));
    }


    /**
     * 减少容器请求数
     * @param  requestId   请求Id
     * @param  containerId 容器id
     * @param  success     函数是否执行成功
     */
    @Override
    public void reduceContainerJob(String requestId, String containerId, boolean success) {
        final Container container = CONTAINER_MAP.get(containerId);
        if(null == container){
            return;
        }
        if(!success){
            container.setEnough(false);
        }
        container.removeRunJob(requestId);
    }
    /**
     * 刷新容器状态
     * @param nodeId               节点id
     * @param containerStatValList 容器状态列表
     */
    @Override
    public void refreshContainerStat(String nodeId, boolean nodeOverload, List<ContainerStatVal> containerStatValList) {
        final Set<Container> containers = NODE_CONTAINER_MAP.get(nodeId);
        if(CollectionUtils.isEmpty(containers)){
            return;
        }
        final Map<String, ContainerStatVal> containerStatValMap =
                containerStatValList.stream().collect(Collectors.toMap(ContainerStatVal::getContainerId, o -> o));

         doRefreshContainerStat(nodeOverload, containerStatValMap, containers);
    }


    /**
     * 添加容器
     * @param container 容器
     */
    @Override
    public void addContainer(Container container) {
        writeLock(container, this::doAddContainer);

    }


    /**
     * 清理节点上的空闲容器
     * @param  nodeId 节点id
     * @return 被清楚的容器id列表
     */
    @Override
    public NodeCleanVal cleanContainer(String nodeId) {
        return writeLockAndRet(nodeId, this::doCleanContainer);
    }



    @Override
    public int countScaleFuncContainer(String funcName) {
        final Set<Container> containerSet = FUNC_CONTAINER_MAP.get(funcName);
        if(CollectionUtils.isEmpty(containerSet)){
            return 0;
        }
        final boolean needScale =
                containerSet.stream()
                            .allMatch(container ->
                                    container.needScale(needScaleCpuUsageRatio, needScaleMemUsageRatio));
        if(!needScale){
            return 0;
        }
        return NumberUtil.mul(containerSet.size(), NumberUtil.sub(expandDivisor, 1)).intValue();
    }

    @Override
    public Set<String> getAllFuncName() {
        return FUNC_CONTAINER_MAP.keySet();
    }

    @Override
    public Optional<Set<Container>> getContainerByNodeId(String nodeId) {
        final Set<Container> containers = NODE_CONTAINER_MAP.get(nodeId);
        if(CollectionUtils.isEmpty(containers)){
            return Optional.empty();
        }
        return Optional.of(Collections.synchronizedSet(containers));
    }

    //*******************************************addContainer***********************************************************
    /**
     * 添加容器
     * @param container 容器
     */
    private void doAddContainer(Container container) {
        // funcName : Container
        final Set<Container> funcContainerSet =
                FUNC_CONTAINER_MAP.computeIfAbsent(container.getFuncName(),
                        v -> new ConcurrentSkipListSet<>(Comparator.comparingInt(Container::getNodeOrder)));
        funcContainerSet.add(container);
        // 节点Id : <funcName : Container>
        final Map<String, Set<Container>> funcContainerMap =
                NODE_FUNC_CONTAINER_MAP.computeIfAbsent(container.getNodeId(), s -> Maps.newConcurrentMap());
        final Set<Container> containers =
                funcContainerMap.computeIfAbsent(container.getFuncName(),
                        v -> new ConcurrentSkipListSet<>(Comparator.comparingInt(Container::getReqJobSize)));
        containers.add(container);
        //  containerId : Container
        CONTAINER_MAP.put(container.getContainerId(), container);
        // 节点Id : Container
        final Set<Container> nodeContainerSet =
                NODE_CONTAINER_MAP.computeIfAbsent(container.getNodeId(), v -> new ConcurrentHashSet<>());
        nodeContainerSet.add(container);
    }
    //*******************************************refreshContainerStat***************************************************
    /**
     * 刷新容器状态
     * @param nodeOverload         是否过载
     * @param containerStatValMap  容器状态map
     * @param containers           容器列表
     */
    private void doRefreshContainerStat(boolean nodeOverload,
                                        Map<String, ContainerStatVal> containerStatValMap,
                                        Set<Container> containers) {
        containers.forEach(container ->
                container.refreshEnough(nodeOverload,
                                        containerStatValMap.get(container.getContainerId()),
                                        maxCpuUsageRatio,
                                        maxMemUsageRatio));
    }
    //*******************************************getAndUseContainer*****************************************************

    /**
     * 获取并使用容器
     * @param requestId       请求Id
     * @param functionInfoVal 函数信息值对象
     * @param containers      容器列表
     * @return 返回使用的容器
     */
    private Optional<Container> doGetAndUseContainer(String requestId, FunctionInfoVal functionInfoVal, Set<Container> containers) {
        if(CollectionUtils.isEmpty(containers)){
            return Optional.empty();
        }
        for (Container container : containers) {
            if(!container.addRunJob(requestId)){
                continue;
            }
            if(log.isDebugEnabled()){
                log.debug("[getAndUseContainer],申请容器,funcName: {}, containerId : {}",
                        functionInfoVal.getName(), container.getContainerId());
            }
            return Optional.of(container);
        }
        return Optional.empty();
    }
    //*********************************************cleanContainer*******************************************************

    private NodeCleanVal doCleanContainer(String nodeId) {
        final Set<Container> containers = NODE_CONTAINER_MAP.get(nodeId);
        if(CollectionUtils.isEmpty(containers)){
            final NodeCleanVal result = NodeCleanVal.of(nodeId, Collections.emptyList());
            result.setNeedRemoveNode(true);
            return result;
        }
        final List<String> removeContainerIdList = Lists.newArrayList();

        for (Container next : containers) {
            if (!next.canClean(maxDelayCleanTime)) {
                continue;
            }
            containers.remove(next);

            removeFuncContainerMap(next);

            removeNodeFuncContainerMap(next);

            CONTAINER_MAP.remove(next.getContainerId());
            removeContainerIdList.add(next.getContainerId());
        }

        final NodeCleanVal result = NodeCleanVal.of(nodeId, removeContainerIdList);
        if(CollectionUtils.isEmpty(containers)){
            NODE_CONTAINER_MAP.remove(nodeId);
            result.setNeedRemoveNode(true);
        }
        return result;
    }

    /**
     * 删除 node : <func: Container> 中的容器
     * @param container 要删除的容器
     */
    private void removeNodeFuncContainerMap(Container container) {
        final Map<String, Set<Container>> funcContainerMap = NODE_FUNC_CONTAINER_MAP.get(container.getNodeId());
        if(MapUtils.isEmpty(funcContainerMap)){
            return;
        }
        final Set<Container> nodeContainerSet = funcContainerMap.get(container.getFuncName());
        if(CollectionUtils.isEmpty(nodeContainerSet)){
            return;
        }

        nodeContainerSet.remove(container);
        if(CollectionUtils.isEmpty(nodeContainerSet)){
            funcContainerMap.remove(container.getFuncName());
        }
        if(MapUtils.isEmpty(funcContainerMap)){
            funcContainerMap.remove(container.getNodeId());
        }
    }
    /**
     * 删除 func: Container 中的容器
     * @param container 要删除的容器
     */
    private void removeFuncContainerMap(Container container) {
        final Set<Container> containerSet = FUNC_CONTAINER_MAP.get(container.getFuncName());
        if(CollectionUtils.isEmpty(containerSet)){
            return;
        }
        containerSet.remove(container);
        if(CollectionUtils.isEmpty(containerSet)){
            FUNC_CONTAINER_MAP.remove(container.getFuncName());
        }
    }
    //********************************************lock******************************************************************

    /**
     * 加写锁 并返回数据
     * @param t        入参
     * @param function 执行函数
     * @param <T>      入参泛型
     */
    private <T, R> R writeLockAndRet(T t, Function<T, R> function) {
        final long writeLock = STAMPED_LOCK.writeLock();
        try {
            return function.apply(t);
        }finally {
            STAMPED_LOCK.unlock(writeLock);
        }
    }
    /**
     * 加写锁
     * @param t        入参
     * @param consumer 执行函数
     * @param <T>      入参泛型
     */
    private <T> void writeLock(T t, Consumer<T> consumer) {
        final long writeLock = STAMPED_LOCK.writeLock();
        try {
            consumer.accept(t);
        }finally {
            STAMPED_LOCK.unlock(writeLock);
        }
    }
    /**
     * 加读锁
     * @param  t        入参
     * @param  function 执行函数
     * @return 回参
     */
    private Optional<Container> readLock(FunctionInfoVal t,
                                         Function<FunctionInfoVal, Optional<Container>> function) {
        // 乐观读
        final long optimismStamp = STAMPED_LOCK.tryOptimisticRead();
        final Optional<Container> apply = function.apply(t);
        if (STAMPED_LOCK.validate(optimismStamp)){
            return apply;
        }
        // 悲观读
        final long pessimismStamp = STAMPED_LOCK.readLock();
        try {
            return function.apply(t);
        }finally {
            STAMPED_LOCK.unlock(pessimismStamp);
        }
    }
}
