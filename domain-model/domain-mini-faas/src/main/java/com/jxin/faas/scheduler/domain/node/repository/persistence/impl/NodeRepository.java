package com.jxin.faas.scheduler.domain.node.repository.persistence.impl;

import com.jxin.faas.scheduler.domain.node.repository.mapper.NodeMapper;
import com.jxin.faas.scheduler.domain.node.repository.persistence.INodeRepository;
import com.jxin.faas.scheduler.domain.node.repository.table.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 节点持久层
 * @author Jxin
 * @version 1.0
 * @since 2020/9/3 20:34
 */
@Repository
public class NodeRepository implements INodeRepository {
    private final NodeMapper nodeMapper;

    @Autowired
    public NodeRepository(NodeMapper nodeMapper) {
        this.nodeMapper = nodeMapper;
    }

    @Override
    public void save(Node node) {
        nodeMapper.insertSelective(node);
    }

    @Override
    public void remove(String nodeId) {
        nodeMapper.deleteByNodeId(nodeId);
    }

    @Override
    public void update(Node node) {
        nodeMapper.updateByNodeId(node, node.getNodeId());
    }
    @Override
    public Optional<Node> getAndUseNode(long memSize, BigDecimal cpuRate) {
        final Node node =
                nodeMapper.findByIdleMemSizeGreaterThanAndCpuUsageRatioLessThanOrderByOrder(memSize, cpuRate);
        if(node == null){
            return Optional.empty();
        }

        final int count = nodeMapper.updateIdleMemSizeByNodeIdAndIdleMemSize(node.getNodeId(), memSize);
        if(count != 1){
            return getAndUseNode(memSize, cpuRate);
        }
        return Optional.of(node);
    }

    @Override
    public int countUsableNode(BigDecimal memRate, BigDecimal cpuRate) {
        return nodeMapper.countByCpuUsageRatioAndMemUsageRatioLessThan(memRate, cpuRate);
    }

    @Override
    public List<String> nodeIdList() {
        return nodeMapper.findNodeId();
    }
}
