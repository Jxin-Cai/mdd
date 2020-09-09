package com.jxin.faas.scheduler.service.acl.resourcemanager;

import com.jxin.faas.scheduler.repository.table.Node;
import resourcemanagerproto.ResourceManagerGrpc;

import java.util.Optional;

/**
 * 资源管理器防腐层
 * @author Jxin
 * @version 1.0
 * @since 2020/7/20 20:35
 */
public interface IResourceManagerAcl {

    void setResourceManagerStub(ResourceManagerGrpc.ResourceManagerBlockingStub resourceManagerStub);
    /**
     * 申请节点
     * @param  requestId 请求Id
     * @param  accountId 用户Id
     * @return 节点信息 值对象
     */
    Optional<Node> reserveNode(String requestId, String accountId);

    /**
     * 释放节点
     * @param  requestId 请求Id
     * @param  nodeId    节点Id
     */
    void releaseNode(String requestId, String nodeId);

}
