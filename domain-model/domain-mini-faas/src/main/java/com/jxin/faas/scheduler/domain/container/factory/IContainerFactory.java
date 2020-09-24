package com.jxin.faas.scheduler.domain.container.factory;

import com.jxin.faas.scheduler.domain.container.dmo.entity.Container;
import com.jxin.faas.scheduler.domain.container.dmo.val.FuncVal;
import com.jxin.faas.scheduler.domain.container.dmo.val.NodeVal;
import com.jxin.faas.scheduler.domain.container.repository.table.ContainerDO;

/**
 * 容器工厂类
 * @author Jxin
 * @version 1.0
 * @since 2020/9/15 17:23
 */
public interface IContainerFactory {
    /**
     * 创建容器实体
     * @param  containerDO 容器 持久对象
     * @return 容器 实体
     */
    Container createContainer(ContainerDO containerDO);
    /**
     * 创建容器实体
     * @param  containerId 容器Id
     * @param  nodeVal     节点值对象
     * @param  funcVal     函数值对象
     * @return 容器 实体
     */
    Container createContainer(String containerId, NodeVal nodeVal, FuncVal funcVal);

    /**
     * 创建容器持久对象
     * @param  container 容器 实体
     * @return 容器 持久对象
     */
    ContainerDO createContainerDO(Container container);

}
