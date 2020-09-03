package com.jxin.faas.scheduler.application.service;

/**
 * 归还容器 服务
 * @author Jxin
 * @version 1.0
 * @since 2020/7/20 22:05
 */
public interface IReturnContainerService {
    /**
     * 归还容器
     * @param  requestId       请求Id
     * @param  containerId     容器Id
     * @param  durationTime    持续时间(ns)
     * @param  memoryUsageSize 使用内存
     * @param  retCode         响应编码
     */
    void returnContainer(String requestId,
                         String containerId,
                         Long durationTime,
                         Long memoryUsageSize,
                         String retCode);
}
