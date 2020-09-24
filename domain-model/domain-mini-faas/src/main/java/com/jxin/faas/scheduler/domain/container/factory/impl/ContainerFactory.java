package com.jxin.faas.scheduler.domain.container.factory.impl;

import com.jxin.faas.scheduler.domain.container.assembler.IContainerConv;
import com.jxin.faas.scheduler.domain.container.dmo.entity.Container;
import com.jxin.faas.scheduler.domain.container.dmo.val.FuncVal;
import com.jxin.faas.scheduler.domain.container.dmo.val.NodeVal;
import com.jxin.faas.scheduler.domain.container.factory.IContainerFactory;
import com.jxin.faas.scheduler.domain.container.factory.INodeFactory;
import com.jxin.faas.scheduler.domain.container.repository.table.ContainerDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 容器工厂类
 * @author Jxin
 * @version 1.0
 * @since 2020/9/15 17:30
 */
@Component
public class ContainerFactory implements IContainerFactory {
    private final INodeFactory nodeFactory;
    private final IContainerConv containerConv;
    @Autowired
    public ContainerFactory(INodeFactory nodeFactory, IContainerConv containerConv) {
        this.nodeFactory = nodeFactory;
        this.containerConv = containerConv;
    }

    @Override
    public Container createContainer(ContainerDO containerDO) {
        final NodeVal nodeVal = nodeFactory.createNodeVal(containerDO);
        return Container.of(containerDO.getContainerId(), nodeVal);
    }
    @Override
    public Container createContainer(String containerId, NodeVal nodeVal, FuncVal funcVal) {
        return Container.of(containerId, nodeVal, funcVal);
    }

    @Override
    public ContainerDO createContainerDO(Container container) {
        return containerConv.domain2Do(container);
    }

}
