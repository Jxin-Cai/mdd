package com.jxin.faas.scheduler.domain.entity.val;

import lombok.Data;

import java.util.List;

/**
 * node清理值对象
 * @author Jxin
 * @version 1.0
 * @since 2020/7/28 20:21
 */
@Data
public class NodeCleanVal {
    /**节点Id*/
    private String nodeId;
    /**是否需要清楚当前node*/
    private boolean needRemoveNode;
    /**清楚的容器Id列表*/
    private List<String> removeContainerIdList;

    private NodeCleanVal(String nodeId, boolean needRemoveNode, List<String> removeContainerIdList) {
        this.nodeId = nodeId;
        this.needRemoveNode = needRemoveNode;
        this.removeContainerIdList = removeContainerIdList;
    }

    public NodeCleanVal() {
    }

    public static NodeCleanVal of(String nodeId, List<String> removeContainerIdList){
        return new NodeCleanVal(nodeId, false, removeContainerIdList);
    }

    public boolean needRemoveContainer(){
        return !removeContainerIdList.isEmpty();
    }
}
