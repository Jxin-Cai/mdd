package com.jxin.faas.scheduler.domain.container.repository.persistence;

import com.jxin.faas.scheduler.domain.container.repository.table.NodeDO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 节点持久层接口
 * @author Jxin
 * @version 1.0
 * @since 2020/9/3 19:51
 */
public interface INodeRepository {
    /**
     * 保存节点数据
     * @param  node 节点 do
     */
    void save(NodeDO node);
    /**
     * 删除节点数据
     * @param  nodeId 节点Id
     */
    void remove(String nodeId);
    /**
     * 更新节点数据
     * @param  node 节点 do
     */
    void update(NodeDO node);

    /**
     * 获取内存充足的可用节点(根据order排序) ,并使用(扣减内存)
     * @param  memSize 内存大小
     * @param  cpuRate cpu使用比例大小
     * @return 资源充足的 node 节点 do 列表
     */
    Optional<NodeDO> getAndUseNode(long memSize, BigDecimal cpuRate);

    /**
     * 统计可用节点数量
     * @param  memRate 内存比例
     * @param  cpuRate cpu比例
     * @return 可用节点数量
     */
    int countUsableNode(BigDecimal memRate, BigDecimal cpuRate);

    /**
     *
     * 获取所有节点 Id 列表
     * @return 节点 Id 列表
     */
    List<String> nodeIdList();
}
