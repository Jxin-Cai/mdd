package com.jxin.faas.scheduler.infrastructure.util;

import io.grpc.ManagedChannel;

/**
 * 关闭资源的钩子
 * @author Jxin
 * @version 1.0
 * @since 2020/7/24 11:50
 */
public interface IShutdownHook {
    /**
     * 关闭连接资源
     * @param channel 连接
     */
    void shutdownManagedChannelHook(ManagedChannel channel);
}
