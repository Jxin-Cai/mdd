package com.jxin.faas.scheduler.application.service;

import com.jxin.faas.scheduler.domain.entity.val.NodeStatVal;

import java.util.concurrent.CountDownLatch;

/**
 * 刷新单节点服务 接口
 * @author Jxin
 * @version 1.0
 * @since 2020/7/28 17:53
 */
public interface IRefreshOneNodeService {
    /**
     * 异步刷新节点状态
     * @param nodeStatVal    节点状态信息 值对象
     * @param countDownLatch 计数器
     */
    void refreshNodeStatAsync(NodeStatVal nodeStatVal, CountDownLatch countDownLatch);

    /**
     * 刷新节点状态
     * @param nodeStatVal    节点状态信息 值对象
     */
    void refreshNodeStat(NodeStatVal nodeStatVal);
}
