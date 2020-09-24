package com.jxin.faas.scheduler.domain.container.factory;

import com.jxin.faas.scheduler.domain.container.dmo.val.NodeVal;
import com.jxin.faas.scheduler.domain.container.repository.table.ContainerDO;
import com.jxin.faas.scheduler.domain.container.repository.table.NodeDO;

/**
 * 节点工厂类
 * @author Jxin
 * @version 1.0
 * @since 2020/9/15 19:00
 */
public interface INodeFactory {
    /**
     * 创建节点值对象(享元模式)
     * @param  containerDO 容器 持久对象
     * @return 节点值对象
     */
    NodeVal createNodeVal(ContainerDO containerDO);
    /**
     * 创建节点值对象(享元模式)
     * @param  nodeDO 节点 持久对象
     * @return 节点值对象
     */
    NodeVal createNodeVal(NodeDO nodeDO);
}
