package com.jxin.faas.scheduler.interfaces.acl.resourcemanager;

import cn.hutool.core.util.StrUtil;
import com.jxin.faas.scheduler.infrastructure.util.IJsonUtil;
import com.jxin.faas.scheduler.repository.table.Node;
import com.jxin.faas.scheduler.service.acl.nodeservice.INodeServiceAcl;
import com.jxin.faas.scheduler.service.acl.resourcemanager.IResourceManagerAcl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import resourcemanagerproto.*;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 资源管理器防腐层
 * @author Jxin
 * @version 1.0
 * @since 2020/7/20 20:46
 */
@Service
@Slf4j
public class ResourceManagerAcl implements IResourceManagerAcl {

    /**节点申请顺序计数器*/
    private static final AtomicInteger ORDER_COUNT = new AtomicInteger(0);
    private ResourceManagerGrpc.ResourceManagerBlockingStub resourceManagerStub;
    private final INodeServiceAcl nodeServiceAcl;
    private final IJsonUtil jsonUtil;

    @Autowired
    public ResourceManagerAcl(INodeServiceAcl nodeServiceAcl, IJsonUtil jsonUtil) {
        this.nodeServiceAcl = nodeServiceAcl;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public void setResourceManagerStub(ResourceManagerGrpc.ResourceManagerBlockingStub resourceManagerStub) {
        this.resourceManagerStub = resourceManagerStub;
    }

    @Override
    public Optional<Node> reserveNode(String requestId, String accountId) {
        try {
            final ReserveNodeReply reserveNodeReply =
                    resourceManagerStub.reserveNode(ReserveNodeRequest.newBuilder()
                            .setRequestId(requestId)
                            .setAccountId(accountId)
                            .build());
            Assert.notNull(reserveNodeReply, StrUtil.format("[ResourceManager],申请node返回为null,requestId : {}", requestId));
            final NodeDesc node = reserveNodeReply.getNode();
            if(log.isDebugEnabled()){
                log.debug("[reserveNode],获取新的节点,node: {}", jsonUtil.beanJson(node));
            }
            // 添加nodeservice的桩
            nodeServiceAcl.addNodeServiceStub(node.getId(), node.getAddress(), node.getNodeServicePort());
            return Optional.of(Node.of(node, ORDER_COUNT.incrementAndGet()));
        }catch (IllegalArgumentException e){
            log.warn("[createNode],获取节点发生业务异常,errMsg: {}", e.getMessage());
            return Optional.empty();
        }catch (Exception e){
            log.warn("[createNode],获取节点发生系统异常errMsg: {}", e.getMessage());
            return Optional.empty();
        }
    }
    @Override
    public void releaseNode(String requestId, String nodeId) {
        resourceManagerStub.releaseNode(ReleaseNodeRequest.newBuilder()
                                                          .setRequestId(requestId)
                                                          .setId(nodeId).build());
    }

}
