package com.jxin.faas.scheduler.domain.container.dmo.val;

import com.jxin.faas.scheduler.domain.container.repository.table.ContainerDO;
import com.jxin.faas.scheduler.domain.container.repository.table.NodeDO;
import lombok.Getter;

/**
 * 节点值对象
 * 注: 无论是类或者字段上,加了final其实在测试时都很麻烦.但不加有不足以表示其严格的语义.
 * @author Jxin
 * @version 1.0
 * @since 2020/9/15 17:14
 */
@Getter
public class NodeVal {
    /**节点id*/
    private final String nodeId;
    /**地址*/
    private final String address;
    /**端口号*/
    private final Integer port;

    public NodeVal(String nodeId, String address, Integer port) {
        this.nodeId = nodeId;
        this.address = address;
        this.port = port;
    }

    public static NodeVal of(ContainerDO containerDO){
        return new NodeVal(containerDO.getNodeId(), containerDO.getAddress(), containerDO.getPort());
    }
    public static NodeVal of(NodeDO nodeDO){
        return new NodeVal(nodeDO.getNodeId(), nodeDO.getAddress(), nodeDO.getPort());
    }
}
