package com.jxin.faas.scheduler.application.service.impl;

import com.jxin.faas.scheduler.application.service.IAcquireContainerService;
import com.jxin.faas.scheduler.application.service.ISchedulerCoreService;
import com.jxin.faas.scheduler.domain.entity.dmo.Container;
import com.jxin.faas.scheduler.domain.entity.val.FunctionInfoVal;
import com.jxin.faas.scheduler.domain.util.IJsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import schedulerproto.AcquireContainerReply;
import schedulerproto.AcquireContainerRequest;
import schedulerproto.FunctionConfig;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 申请容器服务
 * @author Jxin
 * @version 1.0
 * @since 2020/7/20 21:48
 */
@Service
@Slf4j
public class AcquireContainerService implements IAcquireContainerService {
    /**请求数计数器*/
    private static final AtomicInteger ACQUIRE_COUNT = new AtomicInteger(0);
    private final ISchedulerCoreService schedulerCoreService;
    private final IJsonUtil jsonUtil;

    @Autowired
    public AcquireContainerService(ISchedulerCoreService schedulerCoreService, IJsonUtil jsonUtil) {
        this.schedulerCoreService = schedulerCoreService;
        this.jsonUtil = jsonUtil;
    }

    /**
     * 申请容器
     * @param  acquireContainerReqDTO 申请容器 请求 dto
     * @return 申请容器 响应 dto
     */
    @Override
    public AcquireContainerReply acquireContainer(AcquireContainerRequest acquireContainerReqDTO) {
        if (log.isDebugEnabled()){
            log.debug("[acquireContainer],请求计数器,当前请求数: {}", ACQUIRE_COUNT.incrementAndGet());
        }
        final String functionName = acquireContainerReqDTO.getFunctionName();
        FunctionInfoVal functionInfo = getFunctionInfo(acquireContainerReqDTO, functionName);

        final Container container = schedulerCoreService.getContainer(acquireContainerReqDTO.getRequestId(),
                acquireContainerReqDTO.getAccountId(), functionInfo);
        if (log.isDebugEnabled()){
            log.debug("[acquireContainer],请求回参,容器: {}", jsonUtil.beanJson(container));
        }
        return AcquireContainerReply.newBuilder()
                                    .setNodeId(container.getNodeId())
                                    .setNodeAddress(container.getAddress())
                                    .setNodeServicePort(container.getPort())
                                    .setContainerId(container.getContainerId())
                                    .build();
    }

    /**
     * 获取函数信息值对象
     * @param  acquireContainerReqDTO 申请容器 请求 dto
     * @param  functionName            函数名
     * @return 函数信息值对象
     */
    private FunctionInfoVal getFunctionInfo(AcquireContainerRequest acquireContainerReqDTO, String functionName) {
        final FunctionConfig functionConfig = acquireContainerReqDTO.getFunctionConfig();
        return FunctionInfoVal.of(functionName,
                                  functionConfig.getTimeoutInMs(),
                                  functionConfig.getMemoryInBytes(),
                                  functionConfig.getHandler());
    }
}
