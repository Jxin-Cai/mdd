package com.jxin.faas.scheduler.domain.service;

import com.jxin.faas.scheduler.domain.entity.dmo.Container;
import com.jxin.faas.scheduler.domain.entity.val.ContainerStatVal;
import com.jxin.faas.scheduler.domain.entity.val.FunctionInfoVal;
import com.jxin.faas.scheduler.domain.entity.val.NodeCleanVal;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 容器管理器 接口
 * @author Jxin
 * @version 1.0
 * @since 2020/7/28 14:17
 */
public interface IContainerManager {
    /**
     * 获取并使用容器
     * @param  requestId       请求Id
     * @param  nodeId          节点Id
     * @param  functionInfoVal 函数信息值对象
     * @return 容器领域对象
     */
    Optional<Container> getAndUseContainerByNode(String requestId, String nodeId, FunctionInfoVal functionInfoVal);
    /**
     * 获取并使用容器
     * @param  requestId       请求Id
     * @param  functionInfoVal 函数信息值对象
     * @return 容器领域对象
     */
    Optional<Container> getAndUseContainer(String requestId, FunctionInfoVal functionInfoVal);

    /**
     * 减少容器请求数
     * @param  requestId   请求Id
     * @param  containerId 容器id
     * @param  success     函数是否执行成功
     */
    void reduceContainerJob(String requestId, String containerId, boolean success);

    /**
     * 刷新容器状态
     * @param nodeId               节点id
     * @param nodeOverload         节点超载
     * @param containerStatValList 容器状态列表
     */
    void refreshContainerStat(String nodeId, boolean nodeOverload, List<ContainerStatVal> containerStatValList);

    /**
     * 添加容器
     * @param container 容器
     */
    void addContainer(Container container);

    /**
     * 清理节点上的空闲容器
     * @param  nodeId 节点id
     * @return 被清楚的容器id列表
     */
    NodeCleanVal cleanContainer(String nodeId);

    /**
     * 计算需要扩容的容器数
     * @param  funcName 方法名
     * @return 需要扩容的容器数
     */
    int countScaleFuncContainer(String funcName);

    /**
     * 获取所有在允许的函数
     * @return 函数名列表
     */
    Set<String> getAllFuncName();

    /**
     * 获取节点下的所有容器
     * @param  nodeId 节点Id
     * @return 节点下的所有容器
     */
    Optional<Set<Container>> getContainerByNodeId(String nodeId);
}
