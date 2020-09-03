package com.jxin.faas.scheduler.application.service;

import schedulerproto.AcquireContainerReply;
import schedulerproto.AcquireContainerRequest;

/**
 * 申请容器服务
 * @author Jxin
 * @version 1.0
 * @since 2020/7/20 21:41
 */
public interface IAcquireContainerService {
    /**
     * 申请容器
     * @param  acquireContainerReqDTO 申请容器 请求 dto
     * @return 申请容器 响应 dto
     */
    AcquireContainerReply acquireContainer(AcquireContainerRequest acquireContainerReqDTO);
}
