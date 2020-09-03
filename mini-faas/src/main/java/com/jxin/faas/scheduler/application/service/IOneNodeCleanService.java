package com.jxin.faas.scheduler.application.service;

import com.jxin.faas.scheduler.domain.entity.dmo.Node;

import java.util.List;

/**
 * 一个节点的清理服务
 * @author Jxin
 * @version 1.0
 * @since 2020/7/28 20:19
 */
public interface IOneNodeCleanService {
    /**
     * 清理节点及其上面的空闲容器资源
     * @param node 节点
     */
    void cleanNodeContainer(Node node);

    /**
     * 删除节点上的容器
     * @param  node                  节点
     * @param  removeContainerIdList 要删除的容器列表
     */
    void removeContainer(Node node, List<String> removeContainerIdList);
}
