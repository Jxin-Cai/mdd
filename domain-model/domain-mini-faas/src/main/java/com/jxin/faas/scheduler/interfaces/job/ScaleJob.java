package com.jxin.faas.scheduler.interfaces.job;

import com.jxin.faas.scheduler.domain.container.service.api.INodeManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 扩容定时任务
 * @author Jxin
 * @version 1.0
 * @since 2020/7/28 20:15
 */
@Component
@Slf4j
public class ScaleJob {
    private final INodeManager nodeManager;

    @Autowired
    public ScaleJob(INodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    @Scheduled(initialDelay = 20000, fixedRate = 3000)
    public void scaleNodeJob() {
        if(log.isDebugEnabled()){
            log.debug("==================定时检查并扩容node开始==================");
        }
        nodeManager.scaleNode();
        if(log.isDebugEnabled()){
            log.debug("==================定时检查并扩容node结束==================");
        }
    }

}
