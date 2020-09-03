package com.jxin.faas.scheduler.domain.service.impl;

import com.google.common.collect.Maps;
import com.jxin.faas.scheduler.domain.entity.dmo.Container;
import com.jxin.faas.scheduler.domain.entity.dmo.Node;
import com.jxin.faas.scheduler.domain.entity.val.FunctionInfoVal;
import com.jxin.faas.scheduler.domain.entity.val.NodeStatVal;
import com.jxin.faas.scheduler.domain.service.INodeManager;
import com.jxin.faas.scheduler.domain.util.IJsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 节点管理器
 * @author Jxin
 * @version 1.0
 * @since 2020/7/28 16:03
 */
@Service
@Slf4j
public class NodeManager implements INodeManager {
    //**********************************************静态变量************************************************************
    /**票据锁*/
    private static final StampedLock STAMPED_LOCK = new StampedLock();
    /**持有的node节点列表*/
    private static final Set<Node> NODE_SET = new ConcurrentSkipListSet<>(Comparator.comparingInt(Node::getOrder));
    /**node节点列表的映射离散表, nodeId : node*/
    private static final Map<String, Node> NODE_MAP = Maps.newConcurrentMap();
    private static final int MAX_NODE_SIZE = 20;
    //**********************************************配置变量************************************************************
    @Value("${nodeManager.maxCpuUsageRatio}")
    private BigDecimal maxCpuUsageRatio;
    @Value("${nodeManager.maxMemUsageRatio}")
    private BigDecimal maxMemUsageRatio;
    @Value("${nodeManager.needScaleMemUsageRatio}")
    private BigDecimal needScaleMemUsageRatio;
    @Value("${nodeManager.needScaleCpuUsageRatio}")
    private BigDecimal needScaleCpuUsageRatio;
    //**********************************************工具****************************************************************
    @Autowired
    private IJsonUtil jsonUtil;

    @Override
    public Set<Node> nodeSet() {
        return Collections.unmodifiableSet(NODE_SET);
    }

    @Override
    public int nodeSize() {
        return NODE_SET.size();
    }

    /**
     * 获取节点
     * @param  needMemSize 需要的内存大小
     * @return 节点领域对象
     */
    @Override
    public Optional<Node> getNode(Long needMemSize) {
        for (Node node : NODE_SET) {
            if (!node.reduceMem(needMemSize)) {
                continue;
            }
            return Optional.of(node);
        }
        return Optional.empty();
    }
    /**
     * 根据节点获取新容器
     * @param  functionInfoVal 函数信息值对象
     * @param  function        执行函数
     * @return 容器
     */
    @Override
    public Optional<Container> getContainer(FunctionInfoVal functionInfoVal,
                                            Function<FunctionInfoVal, Optional<Container>> function) {
        return readLock(functionInfoVal, function);
    }

    /**
     * 添加节点
     * @param node 节点
     */
    @Override
    public void addNode(Node node) {
        writeLock(node, val -> {
            NODE_SET.add(val);
            NODE_MAP.put(val.getId(), val);
            if(log.isDebugEnabled()){
                log.debug("[addNode],添加节点,nodesSize: {}, nodes : {} ", NODE_SET.size(), jsonUtil.beanJson(NODE_SET));
            }
        });
    }

    @Override
    public Node getNodeById(String nodeId) {
        return NODE_MAP.get(nodeId);
    }

    /**
     * 删除节点
     * @param nodeId 节点Id
     */
    @Override
    public void removeNode(String nodeId) {
        writeLock(nodeId, val -> {
            final Node remove = NODE_MAP.remove(nodeId);
            if(remove == null){
                return;
            }
            NODE_SET.remove(remove);
        });
    }
    /**
     * 刷新节点状态
     * @param nodeStatVal 节点状态信息 值对象
     */
    @Override
    public void refreshNodeStat(NodeStatVal nodeStatVal) {
        final Node node = NODE_MAP.get(nodeStatVal.getNodeId());
        if(null == node){
            if(log.isDebugEnabled()){
                log.debug("[refreshNodeStat],nodeId对应的node不存在,刷新失败,nodeId: {} ", nodeStatVal.getNodeId());
            }
            return;
        }
        node.refreshEnough(nodeStatVal, maxCpuUsageRatio, maxMemUsageRatio);
    }
    /**
     *
     * @return 需要扩容节点则返回 true
     */
    @Override
    public boolean needScaleNode() {
        if(CollectionUtils.isEmpty(NODE_SET)){
            return false;
        }
        if(nodesIsMaxSize()){
            return false;
        }
        return NODE_SET.stream().allMatch(node -> node.needScale(needScaleCpuUsageRatio, needScaleMemUsageRatio));
    }

    @Override
    public boolean nodesIsMaxSize() {
        return NODE_SET.size() >= MAX_NODE_SIZE;
    }

    //********************************************lock******************************************************************

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
     * @param  <T>      入参泛型
     * @param  <R>      回参泛型
     * @return 回参
     */
    private <T, R> R readLock(T t, Function<T, R> function) {
        // 乐观读
        final long optimismStamp = STAMPED_LOCK.tryOptimisticRead();
        final R apply = function.apply(t);
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
