package com.jxin.faas.scheduler.infrastructure.executor.properties;


import lombok.Data;

/**
 * 线程池配置类
 * @author Jxin
 * @version 1.0
 * @since 2020/7/21 17:36
 */
@Data
public class ExecutorProperties {
    /**线程池名称*/
    private String id;
    /**核心线程数*/
    private int corePoolSize;
    /**最大线程数*/
    private int maxPoolSize;
    /**队列大小*/
    private int queueCapacity;
    /**保活毫秒数*/
    private int keepAlive;
}
