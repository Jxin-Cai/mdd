package com.jxin.faas.scheduler.infrastructure.health.entity;

import cn.hutool.system.*;
import io.grpc.internal.JsonUtil;
import lombok.Data;
import org.apache.commons.lang3.SystemUtils;

import java.io.InputStreamReader;
import java.io.LineNumberReader;

/**
 *
 * 系统健康类
 * @author Jxin
 * @version 1.0
 * @since 2020/7/25 7:32 下午
 */
@Data
public class SystemHealth {
    /**操作系统名称*/
    private String systemName;
    /**总内存*/
    private Long totalMem;
    /**空闲内存*/
    private Long freeMem;
    /**使用内存*/
    private Long usageMem;
    /**线程数*/
    private Integer totalThread;
    /**cpu使用率*/
    private Double cpuRatio;

    private SystemHealth(String systemName, Long totalMem, Long freeMem, Long usageMem, Integer totalThread, Double cpuRatio) {
        this.systemName = systemName;
        this.totalMem = totalMem;
        this.freeMem = freeMem;
        this.usageMem = usageMem;
        this.totalThread = totalThread;
        this.cpuRatio = cpuRatio;
    }
}
