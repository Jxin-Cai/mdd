package com.jxin.faas.scheduler.domain.container.dmo.entity;

import com.jxin.faas.scheduler.domain.container.dmo.val.FuncVal;
import com.jxin.faas.scheduler.domain.container.dmo.val.NodeVal;
import lombok.Getter;

import java.util.Date;

/**
 * 容器 实体
 * @author Jxin
 * @version 1.0
 * @since 2020/9/15 17:13
 */
@Getter
public class Container {
    /**容器Id*/
    private String containerId;
    /**节点值对象*/
    private NodeVal nodeVal;
    /**函数值对象*/
    private FuncVal funcVal;
    /**超时时间*/
    private Date outtime;

    /**
     * 使用状态:
     * 0 使用 1未使用
     */
    private Boolean enabled;

    public Container(String containerId, NodeVal nodeVal, FuncVal funcVal) {
        this.containerId = containerId;
        this.nodeVal = nodeVal;
        this.funcVal = funcVal;
    }

    public static Container of(String containerId, NodeVal nodeVal){
        return new Container(containerId, nodeVal, null);
    }
    public static Container of(String containerId, NodeVal nodeVal, FuncVal funcVal){
        return new Container(containerId, nodeVal, funcVal);
    }

    public void useContainer(){
        enabled = true;
        outtime = funcVal.getOutTime();
    }
}
