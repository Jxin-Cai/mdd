package com.jxin.faas.scheduler.domain.container.repository.table;

import com.jxin.faas.scheduler.domain.container.dmo.val.FuncVal;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author Jxin
 * @version 1.0
 * @since 2020/9/3 20:29
 */
@Data
@NoArgsConstructor
public class FuncDO {
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

    public FuncDO(String name, Long memorySize, String handler, Integer timeout) {
        this.name = name;
        this.memorySize = memorySize;
        this.handler = handler;
        this.timeout = timeout;
    }
    public static FuncDO of(FuncVal funcVal){
        return new FuncDO(funcVal.getName(), funcVal.getMemorySize(), funcVal.getHandler(), funcVal.getTimeout());
    }

}