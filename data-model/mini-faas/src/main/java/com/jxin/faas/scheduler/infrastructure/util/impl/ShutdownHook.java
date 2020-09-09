package com.jxin.faas.scheduler.infrastructure.util.impl;

import com.jxin.faas.scheduler.infrastructure.util.IShutdownHook;
import io.grpc.ManagedChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 关闭资源的钩子
 * @author Jxin
 * @version 1.0
 * @since 2020/7/24 11:52
 */
@Component
@Slf4j
public class ShutdownHook implements IShutdownHook {
    @Override
    public void shutdownManagedChannelHook(ManagedChannel channel) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
            log.info("[关闭服务],回收grpc连接");
        }));
    }
}
