package com.jxin.faas.scheduler.domain.container.assembler;

import com.jxin.faas.scheduler.domain.container.dmo.entity.Container;
import com.jxin.faas.scheduler.domain.container.repository.table.ContainerDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import schedulerproto.AcquireContainerReply;

/**
 * 容器实体 转换器
 * @author Jxin
 * @version 1.0
 * @since 2020/9/22 18:50
 */
@Mapper(componentModel = "spring")
public interface IContainerConv {
    /**
     * 实体转dto
     * @param  container 容器实体
     * @return dto
     */
    @Mapping(source = "nodeVal.nodeId", target = "nodeId")
    @Mapping(source = "nodeVal.address", target = "nodeAddress")
    @Mapping(source = "nodeVal.port", target = "nodeServicePort")
    AcquireContainerReply domain2Dto(Container container);

    @Mapping(source = "nodeVal.nodeId", target = "nodeId")
    @Mapping(source = "nodeVal.address", target = "address")
    @Mapping(source = "nodeVal.port", target = "port")
    @Mapping(source = "funcVal.name", target = "funcName")
    ContainerDO domain2Do(Container container);
}
