package com.jxin.faas.scheduler.application.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.jxin.faas.scheduler.application.acl.nodeservice.INodeServiceAcl;
import com.jxin.faas.scheduler.application.service.IMoveReqContainerService;
import com.jxin.faas.scheduler.domain.entity.dmo.Container;
import com.jxin.faas.scheduler.domain.entity.dmo.Function;
import com.jxin.faas.scheduler.domain.entity.dmo.Node;
import com.jxin.faas.scheduler.domain.entity.val.FunctionInfoVal;
import com.jxin.faas.scheduler.domain.service.IContainerManager;
import com.jxin.faas.scheduler.domain.service.IFunctionManager;
import com.jxin.faas.scheduler.domain.service.INodeManager;
import com.jxin.faas.scheduler.domain.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

/**
 * 迁移容器请求服务
 * @author Jxin
 * @version 1.0
 * @since 2020/7/29 11:12
 */
@Service
@Slf4j
public class MoveReqContainerService implements IMoveReqContainerService {
    private static final Semaphore MOVE_REQ_LOCK = new Semaphore(1);
    @Value("${moveReq.minLiveTime}")
    private Integer minLiveTime;


    private final IContainerManager containerManager;
    private final INodeManager nodeManager;
    private final IFunctionManager functionManager;
    private final INodeServiceAcl nodeServiceAcl;

    @Autowired
    public MoveReqContainerService(IContainerManager containerManager, INodeManager nodeManager, IFunctionManager functionManager, INodeServiceAcl nodeServiceAcl) {
        this.containerManager = containerManager;
        this.nodeManager = nodeManager;
        this.functionManager = functionManager;
        this.nodeServiceAcl = nodeServiceAcl;
    }

    @Override
    public void moveReq() {
        if(!MOVE_REQ_LOCK.tryAcquire()){
            if(log.isDebugEnabled()){
                log.debug("[moveReq],获取锁失败,跳出逻辑");
            }
            return;
        }
        try {
            doMoveReq();
        }finally {
            MOVE_REQ_LOCK.release();
        }

    }

    /**
     * 执行迁移工作
     */
    private void doMoveReq() {
        final Set<Node> nodes = nodeManager.nodeSet();
        if(nodes.size() <= 1){
            if(log.isDebugEnabled()){
                log.debug("[moveReq],容器数量 <= 1,无需迁移容器请求,跳出逻辑");
            }
            return;
        }
        final Node last = CollUtil.getLast(nodes);
        final Optional<Set<Container>> containerListOpt = containerManager.getContainerByNodeId(last.getId());
        if(!containerListOpt.isPresent()){
            if(log.isDebugEnabled()){
                log.debug("[moveReq],最后一个节点下的容器数量为0,无需迁移容器请求,跳出逻辑, nodeId : {}", last.getId());
            }
            return;
        }
        final Set<Container> containerSet = containerListOpt.get();
        if(log.isDebugEnabled()){
            log.debug("[moveReq], nodeId: {}, 容器数为: {}",
                      last.getId(), containerSet.size());
        }
        final List<Container> needMoveReqContainers =
                containerSet.stream()
                             .filter(container -> container.needMoveReq(minLiveTime))
                             .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(needMoveReqContainers)){
            return;
        }
        if(log.isDebugEnabled()){
            log.debug("[moveReq], nodeId: {}, 需要迁移请求的容器数为: {}",
                    last.getId(), needMoveReqContainers.size());
        }
        for (Container needMoveReqContainer : needMoveReqContainers) {
            final Optional<Function> functionOpt = functionManager.getFunction(needMoveReqContainer.getFuncName());
            if(!functionOpt.isPresent()){
                if(log.isDebugEnabled()){
                    log.debug("[moveReq], 函数不存在,跳过当前函数的请求迁移工作。nodeId: {}, funcName: {}",
                              last.getId(), needMoveReqContainer.getFuncName());
                }
                continue;
            }
            final FunctionInfoVal functionInfoVal = functionOpt.get().getFunctionInfoVal();
            final Optional<Node> newNodeOptional = getNewNode(last, functionInfoVal);
            if(!newNodeOptional.isPresent()){
                return;
            }
            final Node node = newNodeOptional.get();
            try {
                final Optional<Container> containerOptional = nodeServiceAcl.loopGetContainer(IdUtil.getRequestId(),
                        functionInfoVal, node, 1);
                if(!containerOptional.isPresent()){
                    node.setEnough(false);
                    continue;
                }
                final Container container = containerOptional.get();
                containerManager.addContainer(container);
            }finally {
                node.release();
            }

        }
    }

    /**
     * 获取一个非 last节点的新节点
     * @param  last            最后一个节点
     * @param  functionInfoVal 函数信息值对象
     * @return 节点
     */
    private Optional<Node> getNewNode(Node last, FunctionInfoVal functionInfoVal) {
        final Optional<Node> nodeOptional = nodeManager.getNode(functionInfoVal.getMemorySize());
        if(!nodeOptional.isPresent()){
            if(log.isDebugEnabled()){
                log.debug("[moveReq], 当前持有的节点无法支撑迁移,跳出请求迁移工作。nodeId: {}", last.getId());
            }
            return Optional.empty();
        }
        final Node node = nodeOptional.get();
        if(node.equals(last)){
            if(log.isDebugEnabled()){
                log.debug("[moveReq], 仅剩当前节点能接受新请求,跳出请求迁移工作。nodeId: {}", last.getId());
            }
            last.release();
            return Optional.empty();
        }
        return Optional.of(node);
    }
}
