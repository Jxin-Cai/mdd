package com.jxin.faas.scheduler.application.service;

import com.jxin.faas.scheduler.domain.container.dmo.entity.Container;
import schedulerproto.AcquireContainerRequest;

/**
 * 调度服务 接口
 * @author Jxin
 * @version 1.0
 * @since 2020/9/4 20:38
 */
public interface ISchedulerService {
    /**
     * 获取容器
     * @param  request 获取容器请求参数
     * @return 容器
     */
    Container getContainer(AcquireContainerRequest request);

    /**
     * 完结运行中的任务
     * @param requestId   请求Id
     * @param containerId 容器Id
     * @param success     函数是否执行成功
     */
    void finishRunJob(String requestId, String containerId, boolean success);

}
