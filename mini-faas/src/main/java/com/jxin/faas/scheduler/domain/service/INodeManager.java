package com.jxin.faas.scheduler.domain.service;

import com.jxin.faas.scheduler.domain.entity.dmo.Container;
import com.jxin.faas.scheduler.domain.entity.dmo.Node;
import com.jxin.faas.scheduler.domain.entity.val.FunctionInfoVal;
import com.jxin.faas.scheduler.domain.entity.val.NodeStatVal;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * 节点管理器
 * @author Jxin
 * @version 1.0
 * @since 2020/7/28 15:56
 */
public interface INodeManager {
    /**
     * 获取管理器中所有节点
     * @return 管理器中所有节点
     */
    Set<Node> nodeSet();
    /**节点数量*/
    int nodeSize();
    /**
     * 获取节点
     * @param  needMemSize 需要的内存大小
     * @return 节点领域对象
     */
    Optional<Node> getNode(Long needMemSize);

    /**
     * 根据节点获取新容器
     * @param  functionInfoVal 函数信息值对象
     * @param  function        执行函数
     * @return 容器
     */
    Optional<Container> getContainer(FunctionInfoVal functionInfoVal, Function<FunctionInfoVal, Optional<Container>> function);
    /**
     * 添加节点
     * @param node 节点
     */
    void addNode(Node node);
    /**
     * 根据id获取节点
     * @param nodeId 节点Id
     */
    Node getNodeById(String nodeId);
    /**
     * 删除节点
     * @param nodeId 节点Id
     */
    void removeNode(String nodeId);
    /**
     * 刷新节点状态
     * @param nodeStatVal 节点状态信息 值对象
     */
    void refreshNodeStat(NodeStatVal nodeStatVal);
    /**
     *
     * @return 需要扩容节点则返回 true
     */
    boolean needScaleNode();

    /**
     *
     * @return 节点数达到最大值 返回 true
     */
    boolean nodesIsMaxSize();
}
