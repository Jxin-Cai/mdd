package com.jxin.faas.scheduler.interfaces.grpc;

import com.jxin.faas.scheduler.infrastructure.util.IJsonUtil;
import com.jxin.faas.scheduler.repository.table.Container;
import com.jxin.faas.scheduler.repository.table.Func;
import com.jxin.faas.scheduler.service.api.ISchedulerService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import schedulerproto.*;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 调度器的 RPC服务
 * @author Jxin
 * @version 1.0
 * @since 2020/7/20 17:54
 */
@GrpcService
@Slf4j
public class SchedulerRpc extends SchedulerGrpc.SchedulerImplBase {
    /**请求数计数器*/
    private static final AtomicInteger ACQUIRE_COUNT = new AtomicInteger(0);

    private final ISchedulerService schedulerService;
    private final IJsonUtil jsonUtil;

    @Autowired
    public SchedulerRpc(ISchedulerService schedulerService, IJsonUtil jsonUtil) {
        this.schedulerService = schedulerService;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public void acquireContainer(AcquireContainerRequest request, StreamObserver<AcquireContainerReply> responseObserver) {
        try {
            Assert.notNull(request, "[Scheduler],acquireContainer,入参不能为null");
            if (log.isDebugEnabled()){
                log.debug("[acquireContainer],请求计数器,当前请求数: {}", ACQUIRE_COUNT.incrementAndGet());
            }
            if(log.isDebugEnabled()){
                log.debug("[acquireContainer],请求container,request: {}", jsonUtil.beanJson(request));
            }


            final Container container =
                    schedulerService.getContainer(request.getRequestId(),
                                                  request.getAccountId(),
                                                  warpFunc(request));
            final AcquireContainerReply acquireContainerReply =
                    AcquireContainerReply.newBuilder()
                                         .setNodeId(container.getNodeId())
                                         .setNodeAddress(container.getAddress())
                                         .setNodeServicePort(container.getPort())
                                         .setContainerId(container.getContainerId())
                                         .build();
            responseObserver.onNext(acquireContainerReply);
        }catch (Exception e){
            log.warn(e.getMessage());
            if(log.isDebugEnabled()){
                log.debug(e.getMessage(), e);
            }
            responseObserver.onError(e);
        }
        responseObserver.onCompleted();
    }

    private Func warpFunc(AcquireContainerRequest request) {
        final FunctionConfig functionConfig = request.getFunctionConfig();
        return Func.of(request.getFunctionName(),
                       functionConfig.getMemoryInBytes(),
                       functionConfig.getHandler(),
                       (int)functionConfig.getTimeoutInMs());
    }

    @Override
    public void returnContainer(ReturnContainerRequest request, StreamObserver<ReturnContainerReply> responseObserver) {
        try {
            Assert.notNull(request, "[Scheduler],returnContainer,入参不能为null");
            if(log.isDebugEnabled()){
                log.debug("[returnContainer],归还container,request: {}", jsonUtil.beanJson(request));
            }
            schedulerService.finishRunJob(request.getRequestId(),
                                          request.getContainerId(),
                                          StringUtils.isEmpty(request.getErrorCode()));
            responseObserver.onNext(null);
        }catch (Exception e){
            log.warn(e.getMessage());
            if(log.isDebugEnabled()){
                log.debug(e.getMessage(), e);
            }
            responseObserver.onError(e);
        }
        responseObserver.onCompleted();
    }
}
