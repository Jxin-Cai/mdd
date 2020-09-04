package com.jxin.faas.scheduler.service;

import com.jxin.faas.scheduler.repository.table.Container;
import com.jxin.faas.scheduler.repository.table.Func;

import java.util.Optional;

/**
 * 调度服务 接口
 * @author Jxin
 * @version 1.0
 * @since 2020/9/4 20:38
 */
public interface ISchedulerService {
    /**
     * 获取容器
     * @param  requestId 请求Id
     * @param  accountId 用户id
     * @param  func      函数信息值对象
     * @return 容器
     */
    Container getContainer(String requestId, String accountId, Func func);

    /**
     * 完结运行中的任务
     * @param requestId   请求Id
     * @param containerId 容器Id
     * @param success     函数是否执行成功
     */
    void finishRunJob(String requestId, String containerId, boolean success);

}
