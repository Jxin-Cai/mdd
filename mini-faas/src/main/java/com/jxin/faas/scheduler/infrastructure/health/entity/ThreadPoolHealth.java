package com.jxin.faas.scheduler.infrastructure.health.entity;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * 线程池健康类
 * @author Jxin
 * @version 1.0
 * @since 2020/7/20 21:26
 */
@Data
@ToString
public class ThreadPoolHealth implements Serializable {
    private static final long serialVersionUID = 1204377635041035798L;
    /**当前活动线程数*/
    private Integer activeCount;
    /**当前总线程数*/
    private Integer poolSize;
    /**当前队列任务数*/
    private Integer queueSize;
    /**当前已完成任务数*/
    private Long completedTaskCount;
    /**当前总任务数*/
    private Long totalTaskCount;
    public ThreadPoolHealth() {
        super();
    }
    public ThreadPoolHealth(Integer queueSize,
                            Integer activeCount,
                            Integer poolSize,
                            Long completedTaskCount,
                            Long totalTaskCount) {
        this.queueSize = queueSize;
        this.activeCount = activeCount;
        this.poolSize = poolSize;
        this.completedTaskCount = completedTaskCount;
        this.totalTaskCount = totalTaskCount;
    }
}
