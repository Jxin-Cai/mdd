package com.jxin.faas.scheduler.application.service.impl;

import cn.hutool.core.util.StrUtil;
import com.jxin.faas.scheduler.application.service.ISchedulerCoreService;
import com.jxin.faas.scheduler.application.service.IReturnContainerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 归还容器 服务
 * @author Jxin
 * @version 1.0
 * @since 2020/7/20 22:06
 */
@Service
@Slf4j
public class ReturnContainerService implements IReturnContainerService {
    /**归还计数器*/
    private static final AtomicInteger RETURN_COUNT = new AtomicInteger(0);
    private final ISchedulerCoreService schedulerCoreService;

    @Autowired
    public ReturnContainerService(ISchedulerCoreService schedulerCoreService) {
        this.schedulerCoreService = schedulerCoreService;
    }

    /**
     * 归还容器
     * @param  requestId       请求Id
     * @param  containerId     容器Id
     * @param  durationTime    持续时间(ns)
     * @param  memoryUsageSize 使用内存
     * @param  retCode         响应编码
     */
    @Override
    public void returnContainer(String requestId,
                                String containerId,
                                Long durationTime,
                                Long memoryUsageSize,
                                String retCode) {
        if (log.isDebugEnabled()){
            log.debug("[returnContainer],归还计数器,当前归还数: {}", RETURN_COUNT.incrementAndGet());
        }
        schedulerCoreService.finishRunJob(requestId, containerId, StrUtil.EMPTY.equals(retCode));
    }
}
