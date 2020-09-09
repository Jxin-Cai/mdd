package com.jxin.faas.scheduler.infrastructure.interceptor.client;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

/**
 * grpc全局日志拦截器
 * @author Jxin
 * @version 1.0
 * @since 2020/7/23 11:55
 */
@Slf4j
public class GrpcClientLogInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel channel) {
        final String fullMethodName = method.getFullMethodName();
        if(log.isDebugEnabled()){
            log.debug("[客户端],调用外部服务,funcName: {}", fullMethodName);
        }
        try {
            return channel.newCall(method, callOptions);
        }catch (Exception e){
            log.error("[客户端],调用外部服务发生异常,funcName: {}, errMsg: {}",
                    fullMethodName, e.getMessage(), e);
            throw e;
        }
    }
}
