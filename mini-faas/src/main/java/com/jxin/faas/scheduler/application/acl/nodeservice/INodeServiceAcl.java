package com.jxin.faas.scheduler.application.acl.nodeservice;

import com.jxin.faas.scheduler.domain.entity.dmo.Container;
import com.jxin.faas.scheduler.domain.entity.dmo.Node;
import com.jxin.faas.scheduler.domain.entity.val.FunctionInfoVal;
import com.jxin.faas.scheduler.domain.entity.val.NodeStatVal;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * 节点服务防腐层
 * @author Jxin
 * @version 1.0
 * @since 2020/7/20 20:36
 */
public interface INodeServiceAcl {


    /**
     * 创建容器
     * @param  requestId       请求id
     * @param  node            节点
     * @param  functionInfoVal 函数信息
     * @return container 容器
     */
    Optional<Container> createContainer(String requestId,
                                        Node node,
                                        FunctionInfoVal functionInfoVal);

    /**
     * 循环创建容器
     * @param  requestId       请求id
     * @param  node            节点
     * @param  functionInfoVal 函数信息
     * @param  maxCount        最大循环次数
     * @return container 容器
     */
    Optional<Container> loopGetContainer(String requestId,
                                         FunctionInfoVal functionInfoVal,
                                         Node node,
                                         Integer maxCount);
    /**
     * 删除容器
     * @param  requestId   请求Id
     * @param  nodeId      节点Id
     * @param  containerId 容器Id
     * @param  latch       计数器
     */
    void removeContainer(String requestId, String nodeId, String containerId, CountDownLatch latch);

    /**
     * 获取容器列表
     * @param  nodeId    节点Id
     * @return 节点状态信息 值对象
     */
    Future<NodeStatVal> asyncGetNodeStat(String nodeId);
    /**
     * 获取节点状态信息 值对象
     * @param  nodeId 节点id
     * @return 节点状态信息 值对象
     */
    NodeStatVal getNodeStat(String nodeId);
    /**
     * 添加新节点的桩
     * @param  nodeId 节点Id
     * @param  addr   地址
     * @param  port   端口号
     */
    void addNodeServiceStub(String nodeId, String addr, Long port);
}
