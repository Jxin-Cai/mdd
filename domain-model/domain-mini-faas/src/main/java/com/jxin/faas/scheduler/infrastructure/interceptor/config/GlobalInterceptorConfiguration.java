package com.jxin.faas.scheduler.infrastructure.interceptor.config;

import com.jxin.faas.scheduler.infrastructure.interceptor.client.GrpcClientLogInterceptor;
import com.jxin.faas.scheduler.infrastructure.interceptor.server.GrpcServerLogInterceptor;
import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorConfigurer;
import net.devh.boot.grpc.server.interceptor.GlobalServerInterceptorConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * 配置拦截器
 * @author Jxin
 * @version 1.0
 * @since 2020/7/23 11:54
 */
@Order
// @Configuration(proxyBeanMethods = false)
public class GlobalInterceptorConfiguration {
    @Bean
    public GlobalClientInterceptorConfigurer clientGlobalInterceptorFactory() {
        return registry -> registry.addClientInterceptors(new GrpcClientLogInterceptor());
    }
    @Bean
    public GlobalServerInterceptorConfigurer serverGlobalInterceptorFactory() {
        return registry -> registry.addServerInterceptors(new GrpcServerLogInterceptor());
    }
}
