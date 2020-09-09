package com.jxin.faas.scheduler.service.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 函数信息值对象
 * @author Jxin
 * @version 1.0
 * @since 2020/7/20 17:31
 */
@Data
public class FunctionInfoVal implements Serializable {
    /**函数名*/
    private String name;
    /**超时时间 ms*/
    private Long timeout;
    /**内存大小*/
    private Long memorySize;
    /**执行器*/
    private String handler;

    private FunctionInfoVal(String name, Long timeout, Long memorySize, String handler) {
        this.name = name;
        this.timeout = timeout;
        this.memorySize = memorySize;
        this.handler = handler;
    }

    public FunctionInfoVal() {
    }

    public static FunctionInfoVal of(String name, Long timeout, Long memorySize, String handler){
        return new FunctionInfoVal(name, timeout, memorySize, handler);
    }
}
