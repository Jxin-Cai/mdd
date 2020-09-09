package com.jxin.faas.scheduler.infrastructure.interceptor.server;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

/**
 * grpc全局日志拦截器
 * @author Jxin
 * @version 1.0
 * @since 2020/7/23 11:55
 */
@Slf4j
public class GrpcServerLogInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall,
                                                                 Metadata metadata,
                                                                 ServerCallHandler<ReqT, RespT> serverCallHandler) {
        final String fullMethodName = serverCall.getMethodDescriptor().getFullMethodName();
        if(log.isDebugEnabled()){
            log.debug("[服务端],服务被调用,funcName: {}", fullMethodName);
        }
        try {
            return serverCallHandler.startCall(serverCall, metadata);
        }catch (Exception e){
            log.error("[服务端],发布的服务被调用时发生异常,funcName: {}, errMsg: {}",
                    fullMethodName, e.getMessage(), e);
            throw e;
        }
    }
}
