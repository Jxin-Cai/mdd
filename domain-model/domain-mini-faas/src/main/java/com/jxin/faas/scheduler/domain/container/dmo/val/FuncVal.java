package com.jxin.faas.scheduler.domain.container.dmo.val;

import cn.hutool.core.date.DateUtil;
import com.jxin.faas.scheduler.domain.container.repository.table.FuncDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 函数 值对象
 * @author Jxin
 * @version 1.0
 * @since 2020/7/20 17:31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuncVal implements Serializable {
    /**函数名*/
    private String name;
    /**超时时间 ms*/
    private Integer timeout;
    /**内存大小*/
    private Long memorySize;
    /**执行器*/
    private String handler;

    /**
     * 获取超时时间
     * @return 超时时间
     */
    public Date getOutTime(){
        return DateUtil.offsetMillisecond(new Date(), timeout);
    }

    public static FuncVal of(FuncDO funcDO){
        return new FuncVal(funcDO.getName(), funcDO.getTimeout(), funcDO.getMemorySize(), funcDO.getHandler());
    }
}
