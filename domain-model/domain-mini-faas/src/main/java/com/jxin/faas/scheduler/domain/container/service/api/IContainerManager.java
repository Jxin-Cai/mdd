package com.jxin.faas.scheduler.domain.container.service.api;

import com.jxin.faas.scheduler.domain.container.repository.table.Container;
import com.jxin.faas.scheduler.domain.func.repository.table.Func;

import java.util.Optional;

/**
 * 容器管理器 接口
 * @author Jxin
 * @version 1.0
 * @since 2020/9/4 18:50
 */
public interface IContainerManager {

    /**
     * 扩容容器
     * @param funcName 函数名
     */
    void scaleContainerByFunc(String funcName);
    /**
     * 释放容器
     */
    void releaseContainer();

    /**
     *
     * 根据已有的node获取新容器
     * @param  requestId 请求Id
     * @param  func      函数
     * @return 新容器
     */
    Optional<Container> getContainerByOldNode(String requestId, Func func);
}
