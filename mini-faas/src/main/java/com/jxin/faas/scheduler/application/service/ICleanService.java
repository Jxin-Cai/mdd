package com.jxin.faas.scheduler.application.service;

/**
 * 清理服务
 * @author Jxin
 * @version 1.0
 * @since 2020/7/29 15:29
 */
public interface ICleanService {
    /**
     * 清理节点空闲容器
     */
    void cleanNodeContainer();

    /**
     * 清楚空闲的node
     */
    void cleanIdleNode();
}
