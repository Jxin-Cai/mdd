package com.jxin.faas.scheduler.service.entity;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * 节点状态信息 值对象
 * @author Jxin
 * @version 1.0
 * @since 2020/7/20 20:39
 */
@Data
@ToString
public class NodeStatVal {
    /**节点Id*/
    private String nodeId;
    /**节点内存总大小*/
    private Long memoryAllSize;
    /**节点空闲内存大小*/
    private Long memoryIdleSize;
    /**节点已使用内存大小*/
    private Long memoryUsageSize;
    /**已使用cpu占比*/
    private BigDecimal cpuUsageRatio;



    private NodeStatVal(String nodeId,
                        Long memoryAllSize,
                        Long memoryIdleSize,
                        Long memoryUsageSize,
                        BigDecimal cpuUsageRatio) {
        this.nodeId = nodeId;
        this.memoryAllSize = memoryAllSize;
        this.memoryIdleSize = memoryIdleSize;
        this.memoryUsageSize = memoryUsageSize;
        this.cpuUsageRatio = cpuUsageRatio;

    }

    public static NodeStatVal of(String nodeId,
                                 Long memoryAllSize,
                                 Long memoryIdleSize,
                                 Long memoryUsageSize,
                                 BigDecimal cpuUsageRatio){
        return new NodeStatVal(nodeId, memoryAllSize, memoryIdleSize, memoryUsageSize, cpuUsageRatio);
    }
}
