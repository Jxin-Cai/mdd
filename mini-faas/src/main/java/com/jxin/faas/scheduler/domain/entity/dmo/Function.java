package com.jxin.faas.scheduler.domain.entity.dmo;

import com.google.common.util.concurrent.RateLimiter;
import com.jxin.faas.scheduler.domain.entity.val.FunctionInfoVal;

import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * 函数实例
 * @author Jxin
 * @version 1.0
 * @since 2020/7/28 14:28
 */
@Slf4j
public class Function {
    /**异常次数*/
    private final LongAdder ERR_COUNT = new LongAdder();
    /**函数信息值对象*/
    private final FunctionInfoVal functionInfoVal;

    /**最后请求时间*/
    private Instant latelyTime;

    public Function(FunctionInfoVal functionInfoVal, Instant latelyTime) {
        this.functionInfoVal = functionInfoVal;
        this.latelyTime = latelyTime;
    }


    public static Function of(int awaitTime, int descentIntervalTime, FunctionInfoVal functionInfoVal){
        return new Function( functionInfoVal, null);
    }

    /**
     * 刷新最后调用时间
     */
    @Synchronized
    public boolean refreshLastTime(){
        latelyTime = Instant.now();
        return true;
    }

    /**
     * 添加异常次数
     */
    public void addErrCount(){
        ERR_COUNT.increment();
    }

    /**
     * 创建失败二十次就归结为异常发光法
     * @return 如果是异常方法,返回true
     */
    public boolean isBadFunc(){
        return ERR_COUNT.longValue() > 20;
    }
    /**
     * 判断当前函数需不需要扩容
     * @param  maxIntervalTime 最大间隔时间
     * @return 如果小于间隔时间 , 返回true
     */
    public boolean needScale(int maxIntervalTime){
        return  ChronoUnit.SECONDS.between(latelyTime, Instant.now()) < maxIntervalTime;
    }










    public FunctionInfoVal getFunctionInfoVal() {
        return functionInfoVal;
    }

}
