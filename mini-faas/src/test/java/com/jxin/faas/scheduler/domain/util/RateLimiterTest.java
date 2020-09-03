package com.jxin.faas.scheduler.domain.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 获取锁
 * @author Jxin
 * @version 1.0
 * @since 2020/7/31 11:29
 */
@Slf4j
public class RateLimiterTest {

    @Test
    @SneakyThrows
    public void tryAcquire() {
        final RateLimiter instance = RateLimiter.instance(100, 1000);
        final Executor executor = Executors.newFixedThreadPool(10);
        executor.execute(() -> {
            instance.tryAcquire();
            log.info("线程: {}, 获取锁结束", Thread.currentThread().getName());
        });
        executor.execute(() -> {
            instance.tryAcquire();
            log.info("线程: {}, 获取锁结束", Thread.currentThread().getName());
        });
        executor.execute(() -> {
            instance.tryAcquire();
            log.info("线程: {}, 获取锁结束", Thread.currentThread().getName());
        });
        executor.execute(() -> {
            instance.tryAcquire();
            log.info("线程: {}, 获取锁结束", Thread.currentThread().getName());
        });
        Thread.sleep(3000);
        log.info("==============================================================");
        executor.execute(() -> {
            instance.tryAcquire();
            log.info("线程: {}, 获取锁结束", Thread.currentThread().getName());
        });
        executor.execute(() -> {
            instance.tryAcquire();
            log.info("线程: {}, 获取锁结束", Thread.currentThread().getName());
        });
        executor.execute(() -> {
            instance.tryAcquire();
            log.info("线程: {}, 获取锁结束", Thread.currentThread().getName());
        });
        executor.execute(() -> {
            instance.tryAcquire();
            log.info("线程: {}, 获取锁结束", Thread.currentThread().getName());
        });
        Thread.sleep(10000);
    }
}