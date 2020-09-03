package com.jxin.faas.scheduler.domain.entity.val;

import lombok.Data;

/**
 * 节点信息 值对象
 * @author Jxin
 * @version 1.0
 * @since 2020/7/20 20:39
 */
@Data
public class NodeInfoVal {
    /**节点Id*/
    private String id;
    /**节点地址*/
    private String address;
    /**节点端口*/
    private Long port;
    /**节点内存大小*/
    private Long memorySize;


    private NodeInfoVal(String id, String address, Long port, Long memorySize) {
        this.id = id;
        this.address = address;
        this.port = port;
        this.memorySize = memorySize;
    }

    public NodeInfoVal() {
    }

    public static NodeInfoVal of(String id, String address, Long port, Long memorySize){
        return new NodeInfoVal(id, address, port, memorySize);
    }
}
