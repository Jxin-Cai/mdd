package com.jxin.faas.scheduler.application.service;

import com.jxin.faas.scheduler.domain.entity.dmo.Container;
import com.jxin.faas.scheduler.domain.entity.val.FunctionInfoVal;

/**
 * 调度器核心服务 接口
 * @author Jxin
 * @version 1.0
 * @since 2020/7/21 15:46
 */
public interface ISchedulerCoreService {
    /**
     * 初始化持有的node列表
     */
    void initNodes();
    /**
     * 获取容器
     * @param  requestId       请求Id
     * @param  accountId       用户id
     * @param  functionInfoVal 函数信息值对象
     * @return 容器
     */
    Container getContainer(String requestId, String accountId, FunctionInfoVal functionInfoVal);

    /**
     * 完结运行中的任务
     * @param requestId   请求Id
     * @param containerId 容器Id
     * @param success     函数是否执行成功
     */
    void finishRunJob(String requestId, String containerId, boolean success);

}
