package com.jxin.faas.scheduler.interfaces.job;

import com.jxin.faas.scheduler.service.INodeManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 刷新资源状态的定时任务
 * @author Jxin
 * @version 1.0
 * @since 2020/7/21 19:38
 */
@Component
@Slf4j
public class RefreshResourceStatJob {
    private final INodeManager nodeManager;

    @Autowired
    public RefreshResourceStatJob(INodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }


    @Scheduled(initialDelay = 20000, fixedRate = 200)
    public void refresh() {
        if(log.isDebugEnabled()){
            log.debug("==================定时刷新资源状态开始==================");
        }
        nodeManager.releaseNode();
        if(log.isDebugEnabled()){
            log.debug("==================定时刷新资源状态结束==================");
        }
    }
}
