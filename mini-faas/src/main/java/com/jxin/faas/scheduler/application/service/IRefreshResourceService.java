package com.jxin.faas.scheduler.application.service;

/**
 * 刷新资源服务
 * @author Jxin
 * @version 1.0
 * @since 2020/8/8 10:33
 */
public interface IRefreshResourceService {
    /**
     * 刷新资源状态
     */
    void refreshResourceStat();

    /**
     * 刷新资源状态
     * @param nodeId 节点Id
     */
    void refreshOneNodeStat(String nodeId);
}
