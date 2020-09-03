package com.jxin.faas.scheduler.infrastructure.executor;

import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class ServerThreadPoolConfigurer {
    @Bean
    @Autowired
    public GrpcServerConfigurer build(@Qualifier("serverExecutor") ThreadPoolTaskExecutor serverExecutor){
        return serverBuilder -> serverBuilder.executor(serverExecutor);
    }

}
