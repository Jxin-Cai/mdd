package com.jxin.faas.scheduler.interfaces.grpc;

import com.jxin.faas.scheduler.application.service.IAcquireContainerService;
import com.jxin.faas.scheduler.application.service.IReturnContainerService;
import com.jxin.faas.scheduler.domain.util.IJsonUtil;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import schedulerproto.*;

/**
 * 调度器的 RPC服务
 * @author Jxin
 * @version 1.0
 * @since 2020/7/20 17:54
 */
@GrpcService
@Slf4j
public class SchedulerRpc extends SchedulerGrpc.SchedulerImplBase {
    private final IAcquireContainerService acquireContainerService;
    private final IReturnContainerService returnContainerService;
    private final IJsonUtil jsonUtil;

    @Autowired
    public SchedulerRpc(IAcquireContainerService acquireContainerService, IReturnContainerService returnContainerService, IJsonUtil jsonUtil) {
        this.acquireContainerService = acquireContainerService;
        this.returnContainerService = returnContainerService;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public void acquireContainer(AcquireContainerRequest request, StreamObserver<AcquireContainerReply> responseObserver) {
        try {
            Assert.notNull(request, "[Scheduler],acquireContainer,入参不能为null");
            final AcquireContainerReply acquireContainerReply = acquireContainerService.acquireContainer(request);
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

    @Override
    public void returnContainer(ReturnContainerRequest request, StreamObserver<ReturnContainerReply> responseObserver) {
        try {
            Assert.notNull(request, "[Scheduler],returnContainer,入参不能为null");
            if(log.isDebugEnabled()){
                log.debug("[returnContainer],归还container,request: {}", jsonUtil.beanJson(request));
            }
            returnContainerService.returnContainer(request.getRequestId(),
                                                   request.getContainerId(),
                                                   request.getDurationInNanos(),
                                                   request.getMaxMemoryUsageInBytes(),
                                                   request.getErrorCode());
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
