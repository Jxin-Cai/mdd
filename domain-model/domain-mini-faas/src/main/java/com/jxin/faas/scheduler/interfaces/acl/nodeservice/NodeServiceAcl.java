package com.jxin.faas.scheduler.interfaces.acl.nodeservice;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.jxin.faas.scheduler.domain.container.dmo.val.NodeStatVal;
import com.jxin.faas.scheduler.domain.container.repository.table.Container;
import com.jxin.faas.scheduler.domain.container.service.acl.nodeservice.INodeServiceAcl;
import com.jxin.faas.scheduler.domain.func.repository.table.Func;
import com.jxin.faas.scheduler.domain.node.repository.table.Node;
import com.jxin.faas.scheduler.infrastructure.util.IShutdownHook;
import com.jxin.faas.scheduler.infrastructure.util.IdUtil;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import nodeservoceproto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 节点服务防腐层
 * @author Jxin
 * @version 1.0
 * @since 2020/7/20 21:26
 */
@Service
@Slf4j
public class NodeServiceAcl implements INodeServiceAcl {
    private static final AtomicInteger COUNT = new AtomicInteger(0);

    /**
     * nodeService 桩的列表
     * nodeId : 桩
     */
    private Map<String, NodeServiceGrpc.NodeServiceBlockingStub> nodeServiceBlockingStubMap = Maps.newConcurrentMap();
    private final IShutdownHook shutdownHook;

    @Autowired
    public NodeServiceAcl(IShutdownHook shutdownHook) {
        this.shutdownHook = shutdownHook;
    }


    @Override
    public Optional<Container> createContainer(String requestId,
                                               Node node,
                                               Func func) {
        final String containerId;
        try {
            final NodeServiceGrpc.NodeServiceBlockingStub nodeServiceBlockingStub =
                    nodeServiceBlockingStubMap.get(node.getNodeId());
            Assert.notNull(nodeServiceBlockingStub, "[NodeService],节点客户端桩不存在,nodeId: " + node.getId());

            final FunctionMeta functionMeta = FunctionMeta.newBuilder()
                                                          .setFunctionName(func.getName())
                                                          .setHandler(func.getHandler())
                                                          .setMemoryInBytes(func.getMemorySize())
                                                          .setTimeoutInMs(func.getTimeout())
                                                          .build();

            final CreateContainerReply container =
                    nodeServiceBlockingStub.createContainer(CreateContainerRequest.newBuilder()
                                           .setRequestId(requestId)
                                           .setName(func.getName() + COUNT.incrementAndGet())
                                           .setFunctionMeta(functionMeta)
                                           .build());
            Assert.notNull(container, "[NodeService],创建容器,返回参数为null, requestId: " + requestId);
            containerId = container.getContainerId();
            Assert.notNull(containerId, "[NodeService],创建容器,返回容器Id为containerId, requestId: " + requestId);
        }catch (IllegalArgumentException e){
            log.warn("[createContainer],创建容器发生业务异常, nodeId: {}, errMsg: {}", node.getId(), e.getMessage());
            return Optional.empty();
        }catch (Exception e){
            log.warn("[createContainer],创建容器发生系统异常, nodeId: {}, errMsg: {}", node.getId(), e.getMessage());
            return Optional.empty();
        }

        return Optional.of(Container.of(containerId, node, func));
    }

    @Override
    public Optional<Container> loopGetContainer(String requestId, Func func, Node node, Integer maxCount) {
        for (int i = 0; i < maxCount; i++) {
            final Optional<Container> containerOpt = createContainer(IdUtil.getRequestId(), node, func);
            if(!containerOpt.isPresent()){
                continue;
            }
            return containerOpt;
        }
        return Optional.empty();
    }
    @Async("cleanExecutor")
    @Override
    public void removeContainer(String requestId, String nodeId, String containerId, CountDownLatch latch) {
        final NodeServiceGrpc.NodeServiceBlockingStub nodeServiceBlockingStub =
                nodeServiceBlockingStubMap.get(nodeId);
        Assert.notNull(nodeServiceBlockingStub, "[NodeService],节点客户端桩不存在,nodeId: " + nodeId);
        try {
            nodeServiceBlockingStub.removeContainer(RemoveContainerRequest.newBuilder()
                    .setRequestId(requestId)
                    .setContainerId(containerId)
                    .build());
        }finally {
            latch.countDown();
        }
    }
    @Async("refreshExecutor")
    @Override
    public Future<NodeStatVal> asyncGetNodeStat(String nodeId) {
        try {
            return AsyncResult.forValue(getNodeStat(nodeId));
        }catch (Exception e){
            return AsyncResult.forExecutionException(e);
        }
    }



    @Override
    public void addNodeServiceStub(String nodeId, String addr, Long port) {
        if(nodeServiceBlockingStubMap.containsKey(nodeId)){
            return;
        }

        final ManagedChannel channel = ManagedChannelBuilder.forTarget(StrUtil.format("{}:{}", addr, port))
                                                            .keepAliveWithoutCalls(true)
                                                            .usePlaintext()
                                                            .build();
        final NodeServiceGrpc.NodeServiceBlockingStub nodeServiceBlockingStub = NodeServiceGrpc.newBlockingStub(channel);
        nodeServiceBlockingStubMap.put(nodeId, nodeServiceBlockingStub);

        shutdownHook.shutdownManagedChannelHook(channel);
    }

    /**
     * 获取节点状态信息 值对象
     * @param  nodeId 节点id
     * @return 节点状态信息 值对象
     */
    public NodeStatVal getNodeStat(String nodeId) {
        final NodeServiceGrpc.NodeServiceBlockingStub nodeServiceBlockingStub =
                nodeServiceBlockingStubMap.get(nodeId);
        Assert.notNull(nodeServiceBlockingStub, "[NodeService],节点客户端桩不存在,nodeId: " + nodeId);

        final GetStatsReply stats = nodeServiceBlockingStub.getStats(GetStatsRequest.newBuilder().setRequestId(IdUtil.getRequestId()).build());
        Assert.notNull(stats, "[NodeService],获取容器列表,返回为null, nodeId: " + nodeId);

        final long sum = stats.getContainerStatsListList().stream().mapToLong(ContainerStats::getTotalMemoryInBytes).sum();
        final NodeStats nodeStats = stats.getNodeStats();
        return NodeStatVal.of(nodeId,
                              nodeStats.getTotalMemoryInBytes(),
                              nodeStats.getMemoryUsageInBytes() - sum,
                              nodeStats.getMemoryUsageInBytes(),
                              BigDecimal.valueOf(nodeStats.getCpuUsagePct()));
    }
}
