package com.jxin.faas.scheduler.repository.table;

import lombok.Data;
import lombok.NoArgsConstructor;
import resourcemanagerproto.NodeDesc;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Jxin
 * @version 1.0
 * @since 2020/9/3 20:30
 */
@Data
@NoArgsConstructor
public class Node {
    private Integer id;

    /**
     * 节点Id
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
     * 空闲内存大小
     */
    private Long idleMemSize;

    /**
     * 总内存大小
     */
    private Long totalMemSize;

    /**
     * cpu使用率
     */
    private BigDecimal cpuUsageRatio;

    /**
     * 顺序
     */
    private Integer order;

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

    public Node(String nodeId,
                String address,
                Integer port,
                Long idleMemSize,
                Long totalMemSize,
                Integer order) {
        this.nodeId = nodeId;
        this.address = address;
        this.port = port;
        this.idleMemSize = idleMemSize;
        this.totalMemSize = totalMemSize;
        this.order = order;
    }

    /**
     * 初始化 节点对象
     * @param nodeDesc 节点信息 dto
     * @param order    节点创建顺序
     */
    public static Node of(NodeDesc nodeDesc, Integer order){
        return new Node(nodeDesc.getId(),
                        nodeDesc.getAddress(),
                        (int)nodeDesc.getNodeServicePort(),
                        nodeDesc.getMemoryInBytes(),
                        nodeDesc.getMemoryInBytes(),
                        order);
    }

    /**
     * 初始化 节点对象
     * @param  nodeId        节点Id
     * @param  idleMemSize   空闲内存大小
     * @param  cpuUsageRatio cpu使用大小
     * @return 节点对象
     */
    public static Node of(String nodeId, Long idleMemSize, BigDecimal cpuUsageRatio){
        final Node ret = new Node();
        ret.setNodeId(nodeId);
        ret.setIdleMemSize(idleMemSize);
        ret.setCpuUsageRatio(cpuUsageRatio);
        return ret;
    }
}