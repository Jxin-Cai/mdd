package com.jxin.faas.scheduler.application.service;

import com.jxin.faas.scheduler.domain.entity.val.FunctionInfoVal;

/**
 * 扩容容器的服务
 * @author Jxin
 * @version 1.0
 * @since 2020/7/28 19:48
 */
public interface IScaleContainerService {
    /**
     * 扩容容器
     * @param  functionInfoVal 函数信息值对象
     * @param  count           扩容的数量
     */
    void scaleContainer(FunctionInfoVal functionInfoVal, int count);
}
