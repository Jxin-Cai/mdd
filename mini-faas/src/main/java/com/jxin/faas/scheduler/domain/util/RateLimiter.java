package com.jxin.faas.scheduler.domain.util;

import lombok.SneakyThrows;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 增量限流器
 * @author Jxin
 * @version 1.0
 * @since 2020/7/31 9:55
 */
public class RateLimiter {
    /**增长系数*/
    private static final int GROW_DIVISOR = 4;
    /**增长锁, 同一时间只允许一次增长*/
    private final Semaphore GROW_TOKEN_SEMAPHORE = new Semaphore(1);
    /**读写锁*/
    private final Lock lock;
    /**令牌为空*/
    private final Condition tokenIsEmpty;
    /**信号*/
    private Semaphore semaphore;

    /**最近请求时间*/
    private Instant latelyTime;
    /**持有的队列*/
    private final Deque<Integer> stack;
    /**令牌数*/
    private int tokenCount;
    /**等待时间*/
    private final int awaitTime;
    /**下降间隔时间*/
    private final int descentIntervalTime;

    public RateLimiter(Lock lock, Condition tokenIsEmpty, Semaphore semaphore, Instant latelyTime, Deque<Integer> stack, int tokenCount, int awaitTime, int descentIntervalTime) {
        this.lock = lock;
        this.tokenIsEmpty = tokenIsEmpty;
        this.semaphore = semaphore;
        this.latelyTime = latelyTime;
        this.stack = stack;
        this.tokenCount = tokenCount;
        this.awaitTime = awaitTime;
        this.descentIntervalTime = descentIntervalTime;
    }


    public static RateLimiter instance(int awaitTime, int descentIntervalTime){
        final Lock lock = new ReentrantLock();
        final Condition tokenIsEmpty = lock.newCondition();
        final Deque<Integer> stack = new ConcurrentLinkedDeque<>();
        stack.push(1);
        return new RateLimiter(lock,
                               tokenIsEmpty,
                               new Semaphore(1),
                               Instant.now(),
                               stack,
                               1,
                               awaitTime,
                               descentIntervalTime);
    }
    @SneakyThrows
    public void tryAcquire() {
        lock.lock();
        try {
            cleanStack();
            while (!semaphore.tryAcquire()){
                final int oldSize = stack.size();
                tokenIsEmpty.await(awaitTime, TimeUnit.MILLISECONDS);
                final int newSize = stack.size();
                if(oldSize != newSize){
                    continue;
                }
                // 增长token数
                growTokenCount();
            }
            latelyTime = Instant.now();
        }finally {
            lock.unlock();
        }
    }

    /**
     * 滚动增长token数
     */
    private void growTokenCount() {
        if (!GROW_TOKEN_SEMAPHORE.tryAcquire()){
            return;
        }
        try {
            final Integer peek = stack.peek();
            if(null == peek){
                tokenCount = 1;
                stack.push(1);
            }else {
                tokenCount = stack.peek() * GROW_DIVISOR;
                stack.push(tokenCount);
            }
            semaphore = new Semaphore(tokenCount);
        }finally {
            GROW_TOKEN_SEMAPHORE.release();
        }

    }

    /**
     * 清理栈空间
     * 间隔时间每超过一个 intervalTime,废弃一个最大令牌数量阀值
     */
    private void cleanStack() {
        final long between = ChronoUnit.MILLIS.between(latelyTime, Instant.now());
        final long count = between / descentIntervalTime;
        if(count == 0){
            return;
        }
        for (long i = 0; i < count; i++) {
            stack.pollFirst();
        }
        final Integer peek = stack.peek();
        if(null == peek){
            tokenCount = 1;
            stack.push(1);
        }else {
            tokenCount = peek;
        }
        semaphore = new Semaphore(tokenCount);
    }
}