package com.jxin.faas.scheduler.application.acl.resourcemanager;

import com.jxin.faas.scheduler.domain.entity.dmo.Node;
import com.jxin.faas.scheduler.domain.entity.val.NodeInfoVal;
import resourcemanagerproto.ResourceManagerGrpc;

import java.util.List;
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

    /**
     * 获取使用中的所有节点
     * @param  requestId 请求Id
     * @return 节点信息 列表
     */
    List<NodeInfoVal> getNodesUsage(String requestId);
}
