package com.jxin.faas.scheduler.interfaces.acl.resourcemanager;

import cn.hutool.core.util.StrUtil;
import com.jxin.faas.scheduler.application.acl.nodeservice.INodeServiceAcl;
import com.jxin.faas.scheduler.application.acl.resourcemanager.IResourceManagerAcl;
import com.jxin.faas.scheduler.domain.entity.dmo.Node;
import com.jxin.faas.scheduler.domain.entity.val.NodeInfoVal;
import com.jxin.faas.scheduler.domain.util.IJsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import resourcemanagerproto.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

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
    // @GrpcClient("faas-resource")
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
    @Override
    public List<NodeInfoVal> getNodesUsage(String requestId) {
        final GetNodesUsageReply nodesUsage =
                resourceManagerStub.getNodesUsage(GetNodesUsageRequest.newBuilder()
                                                                      .setRequestId(requestId)
                                                                      .build());
        Assert.notNull(nodesUsage, StrUtil.format("[ResourceManager],查看占用node列表返回为空,requestId : {}", requestId));
       return nodesUsage.getNodesList().stream()
                                       .map(node -> NodeInfoVal.of(node.getId(),
                                                                   node.getAddress(),
                                                                   node.getNodeServicePort(),
                                                                   node.getMemoryInBytes()))
                                       .collect(Collectors.toList());
    }
}
