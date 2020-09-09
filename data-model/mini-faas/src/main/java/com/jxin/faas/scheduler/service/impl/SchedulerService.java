package com.jxin.faas.scheduler.service.impl;

import cn.hutool.core.date.DateUtil;
import com.jxin.faas.scheduler.repository.persistence.IContainerRepository;
import com.jxin.faas.scheduler.repository.persistence.IFuncRepository;
import com.jxin.faas.scheduler.repository.table.Container;
import com.jxin.faas.scheduler.repository.table.Func;
import com.jxin.faas.scheduler.service.api.IContainerManager;
import com.jxin.faas.scheduler.service.api.INodeManager;
import com.jxin.faas.scheduler.service.api.ISchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

/**
 * 调度服务
 * @author Jxin
 * @version 1.0
 * @since 2020/9/4 20:33
 */
@Service
public class SchedulerService implements ISchedulerService {
    /**创建新节点的锁*/
    private static final Object NEW_NODE_LOCK = new Object();

    private final IFuncRepository funcRepository;
    private final IContainerRepository containerRepository;

    private final IContainerManager containerManager;
    private final INodeManager nodeManager;
    @Autowired
    public SchedulerService(IFuncRepository funcRepository, IContainerRepository containerRepository, IContainerManager containerManager, INodeManager nodeManager) {
        this.funcRepository = funcRepository;
        this.containerRepository = containerRepository;
        this.containerManager = containerManager;
        this.nodeManager = nodeManager;
    }

    @Override
    public Container getContainer(String requestId, String accountId, Func func) {
        // 保存函数数据 (当函数不存在时)
        funcRepository.saveWhenNone(func);
        // 从本地已有资源获取容器
        final Optional<Container> containerOpt = getContainerByLocal(requestId, func);
        // 如果本地已有资源获取容器,获取新的节点来获取新的容器
        return containerOpt.orElseGet(() -> getContainerByNewNode(requestId, func));

    }

    @Override
    public void finishRunJob(String requestId, String containerId, boolean success) {
        containerRepository.releaseContainer(containerId);
    }
    //********************************************getContainer**********************************************************
    /**
     *
     * 从本地已有资源获取容器
     * @param  requestId 请求Id
     * @param  func      函数
     * @return 容器
     */
    private Optional<Container> getContainerByLocal(String requestId, Func func) {
        // 从已有的容器中获取
        final Optional<Container> containerOptional =
                containerRepository.getAndUseContainer(func.getName(), DateUtil.offsetMillisecond(new Date(), func.getTimeout()));
        if(containerOptional.isPresent()){
            return containerOptional;
        }

        // 从已有的节点获取新容器(本地数据存储和远程接口事物存在分布式事物,先不处理)
        return containerManager.getContainerByOldNode(requestId, func);
    }

    /**
     *
     * 1.获取新的节点来获取新的容器(本地数据存储和远程接口事物存在分布式事物,先不处理)
     * 2.同一时间只允许一个请求创建新的node
     * @param  requestId 请求Id
     * @param  func      函数
     * @return 容器
     */
    private Container getContainerByNewNode(String requestId, Func func) {
        synchronized (NEW_NODE_LOCK){
            final Optional<Container> containerByLocalOpt = getContainerByLocal(requestId, func);
            if(containerByLocalOpt.isPresent()){
                return containerByLocalOpt.get();
            }
            // 获取新的节点来获取新的容器(本地数据存储和远程接口事物存在分布式事物,先不处理)
            nodeManager.reserveAndSaveNode();
            return getContainerByNewNode(requestId, func);
        }
    }


}
