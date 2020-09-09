package com.jxin.faas.scheduler.repository.persistence;

import com.jxin.faas.scheduler.repository.table.Container;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 容器持久层接口
 * @author Jxin
 * @version 1.0
 * @since 2020/9/3 19:52
 */
public interface IContainerRepository {
    /**
     * 保存容器数据
     * @param  container 容器 do
     */
    void save(Container container);
    /**
     * 删除空闲容器
     * @param nodeId      节点Id
     * @param lastReqTime 最后请求的时间
     */
    List<String> removeIdleContainer(String nodeId, Date lastReqTime);
    /**
     * 删除超时容器
     * @param nodeId  节点Id
     * @param outTime 超时时间
     */
    List<String> removeOutTimeContainer(String nodeId, Date outTime);
    /**
     * 使用容器
     * @param  containerId 容器id
     * @param  outTime 超时时间
     */
    void enableContainer(String containerId, Date outTime);

    /**
     * 释放容器
     * @param  containerId 容器id
     */
    void releaseContainer(String containerId);

    /**
     * 获取并使用容器(根据order和id排序)
     * @param  funcName 函数名
     * @param  outTime 超时时间
     * @return 可用的容器 do
     */
    Optional<Container> getAndUseContainer(String funcName, Date outTime);

    /**
     * 统计指定nodeId的容器数
     * @param  nodeId 节点id
     * @return 容器数量
     */
    int countByNodeId(String nodeId);

    /**
     * 统计指定funcName的容器数
     * @param  funcName 函数名
     * @return 容器数量
     */
    int countByFuncName(String funcName);
}
