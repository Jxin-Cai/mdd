package com.jxin.faas.scheduler.repository.table;

import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Jxin
 * @version 1.0
 * @since 2020/9/3 20:29
 */
@Data
@NoArgsConstructor
public class Func {
    private Integer id;

    /**
     * 函数名
     */
    private String name;

    /**
     * 内存大小
     */
    private Long memorySize;

    /**
     * 执行器
     */
    private String handler;

    /**
     * 超时时间 单位ms
     */
    private Integer timeout;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date modifyTime;

    /**
     * 逻辑删除
     */
    private Boolean deleted;

    public Func(String name, Long memorySize, String handler, Integer timeout) {
        this.name = name;
        this.memorySize = memorySize;
        this.handler = handler;
        this.timeout = timeout;
    }
    public static Func of(String name, Long memorySize, String handler, Integer timeout){
        return new Func(name, memorySize, handler, timeout);
    }
}