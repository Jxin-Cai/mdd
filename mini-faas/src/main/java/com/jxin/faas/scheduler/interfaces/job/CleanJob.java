package com.jxin.faas.scheduler.interfaces.job;

import com.jxin.faas.scheduler.application.service.ICleanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 清理空闲资源定时任务
 * @author Jxin
 * @version 1.0
 * @since 2020/7/21 22:35
 */
@Component
@Slf4j
public class CleanJob {
    private final ICleanService cleanService;

    @Autowired
    public CleanJob(ICleanService cleanService) {
        this.cleanService = cleanService;
    }

    @Scheduled(initialDelay = 20000, fixedRate = 1000)
    public void cleanNodeContainer(){
        if(log.isDebugEnabled()){
            log.debug("==================定时清理空闲资源开始[容器]==================");
        }
        cleanService.cleanNodeContainer();
        if(log.isDebugEnabled()){
            log.debug("==================定时清理空闲资源结束[容器]==================");
        }
    }
    @Scheduled(initialDelay = 20000, fixedRate = 30000)
    public void cleanIdleNode(){
        if(log.isDebugEnabled()){
            log.debug("==================定时清理空闲资源开始[node]==================");
        }
        cleanService.cleanIdleNode();
        if(log.isDebugEnabled()){
            log.debug("==================定时清理空闲资源结束[node]==================");
        }
    }
}
