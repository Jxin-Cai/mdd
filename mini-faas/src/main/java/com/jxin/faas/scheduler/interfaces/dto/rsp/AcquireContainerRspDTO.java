package com.jxin.faas.scheduler.interfaces.dto.rsp;

import lombok.Data;

import java.io.Serializable;

/**
 * 申请容器 响应 dto
 * @author Jxin
 * @version 1.0
 * @since 2020/7/20 17:43
 */
@Data
public class AcquireContainerRspDTO implements Serializable {
    /**节点Id*/
    private String nodeId;
    /**节点地址*/
    private String nodeAddr;
    /**节点端口号*/
    private Integer nodePort;
    /**容器Id*/
    private String containerId;
}
