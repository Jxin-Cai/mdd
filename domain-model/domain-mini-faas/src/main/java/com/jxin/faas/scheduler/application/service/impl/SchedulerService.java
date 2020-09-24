package com.jxin.faas.scheduler.application.service.impl;

import com.jxin.faas.scheduler.domain.container.assembler.IFuncValConv;
import com.jxin.faas.scheduler.application.service.ISchedulerService;
import com.jxin.faas.scheduler.domain.container.dmo.entity.Container;
import com.jxin.faas.scheduler.domain.container.dmo.val.FuncVal;
import com.jxin.faas.scheduler.domain.container.service.IContainerManager;
import com.jxin.faas.scheduler.domain.container.service.IFuncManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import schedulerproto.AcquireContainerRequest;

import java.util.Optional;

/**
 * 调度服务
 * @author Jxin
 * @version 1.0
 * @since 2020/9/4 20:33
 */
@Service
public class SchedulerService implements ISchedulerService {
    private final IFuncManager funcManager;
    private final IFuncValConv funcValConv;
    private final IContainerManager containerManager;

    @Autowired
    public SchedulerService(IFuncManager funcManager, IFuncValConv funcValConv, IContainerManager containerManager) {
        this.funcManager = funcManager;
        this.funcValConv = funcValConv;
        this.containerManager = containerManager;
    }

    @Override
    public Container getContainer(AcquireContainerRequest request) {
        // 保存函数数据 (当函数不存在时)
        final FuncVal funcVal = funcValConv.dto2domain(request);
        funcManager.saveWhenNone(funcVal);
        // 从本地已有资源获取容器
        final Optional<Container> containerOpt = containerManager.getContainerByLocal(request.getRequestId(), funcVal);
        if(containerOpt.isPresent()){
            return containerOpt.get();
        }
        // 如果已有容器不足以支撑请求,则发起一次异步扩容
        containerManager.scaleContainerByFunc(funcVal.getName());
        // 从本地节点创建新的容器
        final Optional<Container> containerByOldNodeOpt = containerManager.getAndSaveContainerByOldNode(request.getRequestId(), funcVal);
        // 从创建新的节点来创建新的容器
        return containerByOldNodeOpt.orElseGet(() -> containerManager.getAndSaveContainerByNewNode(request.getRequestId(), funcVal));
    }

    @Override
    public void finishRunJob(String requestId, String containerId, boolean success) {
        containerManager.finishRunJob(requestId, containerId, success);
    }


}
