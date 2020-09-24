package com.jxin.faas.scheduler.domain.container.repository.table;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Jxin
 * @version 1.0
 * @since 2020/9/4 19:29
 */
@Data
@NoArgsConstructor
public class ContainerDO {
    private Integer id;

    /**
     * 容器Id
     */
    private String containerId;

    /**
     * 节点id
     */
    private String nodeId;

    /**
     * 地址
     */
    private String address;

    /**
     * 端口号
     */
    private Integer port;

    /**
     * 顺序
     */
    private Integer order;

    /**
     * 函数id
     */
    private Integer funId;

    /**
     * 函数名
     */
    private String funcName;

    /**
     * 超时时间
     */
    private Date outtime;

    /**
     * 内存大小
     */
    private Long memSize;

    /**
     * cpu使用率
     */
    private BigDecimal cpuUsageRatio;

    /**
     * 使用状态: 0 使用 1未使用
     */
    private Boolean enabled;

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

    public ContainerDO(String containerId, String nodeId, String address,
                       Integer port, Integer order, Integer funId, String funcName, Long memSize) {
        this.containerId = containerId;
        this.nodeId = nodeId;
        this.address = address;
        this.port = port;
        this.order = order;
        this.funId = funId;
        this.funcName = funcName;
        this.memSize = memSize;
    }

    /**
     *
     * 创建容器实例
     * @param containerId 容器Id
     * @param node        节点
     * @param func        函数
     */
    public static ContainerDO of(String containerId, NodeDO node, FuncDO func) {
        return new ContainerDO(containerId,
                             node.getNodeId(),
                             node.getAddress(),
                             node.getPort(),
                             node.getOrder(),
                             func.getId(),
                             func.getName(),
                             func.getMemorySize());
    }
}