package com.jxin.faas.scheduler.domain.container.factory.impl;

import com.google.common.collect.Maps;
import com.jxin.faas.scheduler.domain.container.dmo.val.NodeVal;
import com.jxin.faas.scheduler.domain.container.factory.INodeFactory;
import com.jxin.faas.scheduler.domain.container.repository.table.ContainerDO;
import com.jxin.faas.scheduler.domain.container.repository.table.NodeDO;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 节点工厂类
 * @author Jxin
 * @version 1.0
 * @since 2020/9/15 19:01
 */
@Component
public class NodeFactory implements INodeFactory {
    /**节点值对象 本地缓存(享元模式)*/
    private static final Map<String, NodeVal> NODE_VAL_CACHE = Maps.newConcurrentMap();
    /**
     * 创建节点值对象(享元模式)
     * @param  containerDO 容器 持久对象
     * @return 节点值对象
     */
    @Override
    public NodeVal createNodeVal(ContainerDO containerDO) {
        return NODE_VAL_CACHE.computeIfAbsent(containerDO.getNodeId(), s -> NodeVal.of(containerDO));
    }

    @Override
    public NodeVal createNodeVal(NodeDO nodeDO) {
        return NODE_VAL_CACHE.computeIfAbsent(nodeDO.getNodeId(), s -> NodeVal.of(nodeDO));
    }
}
