package com.jxin.faas.scheduler.interfaces.job;

import com.jxin.faas.scheduler.service.api.IContainerManager;
import com.jxin.faas.scheduler.service.api.INodeManager;
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
    private final IContainerManager containerManager;
    private final INodeManager nodeManager;

    @Autowired
    public CleanJob(IContainerManager containerManager, INodeManager nodeManager) {
        this.containerManager = containerManager;
        this.nodeManager = nodeManager;
    }


    @Scheduled(initialDelay = 20000, fixedRate = 1000)
    public void cleanIdleContainer(){
        if(log.isDebugEnabled()){
            log.debug("==================定时清理空闲资源开始[容器]==================");
        }
        containerManager.releaseContainer();
        if(log.isDebugEnabled()){
            log.debug("==================定时清理空闲资源结束[容器]==================");
        }
    }
    @Scheduled(initialDelay = 20000, fixedRate = 15000)
    public void cleanIdleNode(){
        if(log.isDebugEnabled()){
            log.debug("==================定时清理空闲资源开始[node]==================");
        }
        nodeManager.releaseNode();
        if(log.isDebugEnabled()){
            log.debug("==================定时清理空闲资源结束[node]==================");
        }
    }
}
