package com.jxin.faas.scheduler.service.api;

import com.jxin.faas.scheduler.repository.table.Node;
import com.jxin.faas.scheduler.service.exec.NodeException;

import java.util.Optional;

/**
 * 节点管理器 接口
 * @author Jxin
 * @version 1.0
 * @since 2020/9/4 20:31
 */
public interface INodeManager {
    /**
     * 扩容节点
     */
    void scaleNode();
    /**
     * 释放节点
     */
    void releaseNode();

    /**
     * 刷新节点值对象
     */
    void refresh();

    /**
     * 获取内存充足的可用内存(根据order排序) ,并使用(扣减内存)
     * @param  memSize 内存大小
     * @return 资源充足的 node 节点 do 列表
     */
    Optional<Node> getAndUseNode(long memSize);

    /**
     * 申请并保存 node
     * @throws NodeException 无法申请新的节点
     */
    void reserveAndSaveNode();
}
