package com.jxin.faas.scheduler.domain.container.service;

import com.jxin.faas.scheduler.domain.container.dmo.entity.Container;
import com.jxin.faas.scheduler.domain.container.dmo.val.FuncVal;

import java.util.Optional;

/**
 * 容器管理器 接口
 * @author Jxin
 * @version 1.0
 * @since 2020/9/4 18:50
 */
public interface IContainerManager {
    /**
     * 从已有容器获取容器
     * @param  requestId 请求Id
     * @param  funcVal   函数信息值对象
     * @return 容器
     */
    Optional<Container> getContainerByLocal(String requestId, FuncVal funcVal);

    /**
     * 根据已有node创建新的容器(并保存到数据库)
     * @param  requestId 请求Id
     * @param  funcVal   函数信息值对象
     * @return 新的容器
     */
    Optional<Container> getAndSaveContainerByOldNode(String requestId, FuncVal funcVal);
    /**
     *
     * 1.获取新的节点来获取新的容器(本地数据存储和远程接口事物存在分布式事物,先不处理)
     * 2.同一时间只允许一个请求创建新的node
     * @param  requestId 请求Id
     * @param  func      函数
     * @return 新的容器
     */
    Container getAndSaveContainerByNewNode(String requestId, FuncVal func);
    /**
     * 完结运行中的任务
     * @param requestId   请求Id
     * @param containerId 容器Id
     * @param success     函数是否执行成功
     */
    void finishRunJob(String requestId, String containerId, boolean success);
    /**
     * 扩容容器
     * @param funcName 函数名
     */
    void scaleContainerByFunc(String funcName);
    /**
     * 释放容器
     */
    void releaseContainer();

}
