package com.jxin.faas.scheduler.application.service;

/**
 * 扩容服务
 * @author Jxin
 * @version 1.0
 * @since 2020/7/28 18:55
 */
public interface IScaleService {
    /**
     * 扩容节点
     */
    void scaleNode();
    /**
     * 扩容方法容器
     */
    void scaleFuncContainer();

}
