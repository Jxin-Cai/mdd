package com.jxin.faas.scheduler.log;

import java.util.Date;

/**
 * 日志实例
 * @author Jxin
 * @version 1.0
 * @since 2020/7/24 14:36
 */
@lombok.Data
class Log {
    /**日志信息*/
    private String log;
    /**打印时间*/
    private Date time;
}
