package com.jxin.faas.scheduler.interfaces.job;

import com.jxin.faas.scheduler.application.service.IMoveReqContainerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 迁移请求定时任务
 * @author Jxin
 * @version 1.0
 * @since 2020/7/29 11:59
 */
// @Service
@Slf4j
public class MoveReqJob {
    private final IMoveReqContainerService moveReqContainerService;

    @Autowired
    public MoveReqJob(IMoveReqContainerService moveReqContainerService) {
        this.moveReqContainerService = moveReqContainerService;
    }

    @Scheduled(initialDelay = 20000, fixedRate = 5000)
    public void clean(){
        if(log.isDebugEnabled()){
            log.debug("==================定时迁移最后一个节点的请求开始==================");
        }
        moveReqContainerService.moveReq();
        if(log.isDebugEnabled()){
            log.debug("==================定时迁移最后一个节点的请求结束==================");
        }
    }
}
