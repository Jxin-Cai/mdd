package com.jxin.faas.scheduler.domain.container.assembler;

import com.jxin.faas.scheduler.domain.container.dmo.val.FuncVal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import schedulerproto.AcquireContainerRequest;

/**
 * 函数值对象 转换器
 * @author Jxin
 * @version 1.0
 * @since 2020/9/15 17:59
 */
@Mapper(componentModel = "spring")
public interface IFuncValConv {
    @Mappings({
            @Mapping(source = "functionName", target = "name"),
            @Mapping(source = "functionConfig.memoryInBytes", target = "memorySize"),
            @Mapping(source = "functionConfig.timeoutInMs", target = "timeout"),
    })
    FuncVal dto2domain(AcquireContainerRequest request);
}
