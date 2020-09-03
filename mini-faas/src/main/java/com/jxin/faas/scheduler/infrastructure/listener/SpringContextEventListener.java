package com.jxin.faas.scheduler.infrastructure.listener;

import com.jxin.faas.scheduler.application.acl.resourcemanager.IResourceManagerAcl;
import com.jxin.faas.scheduler.application.service.ISchedulerCoreService;
import com.jxin.faas.scheduler.domain.util.IShutdownHook;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import resourcemanagerproto.ResourceManagerGrpc;

/**
 * spring上下文的事件监听器
 * @author Jxin
 * @version 1.0
 * @since 2020/7/24 11:22
 */
@Component
@Slf4j
public class SpringContextEventListener {
    private final IResourceManagerAcl resourceManagerAcl;
    private final IShutdownHook shutdownHook;

    @Autowired
    public SpringContextEventListener(IResourceManagerAcl resourceManagerAcl, IShutdownHook shutdownHook) {
        this.resourceManagerAcl = resourceManagerAcl;
        this.shutdownHook = shutdownHook;
    }

    /**
     * 监听spring上下文加载完成事件
     * @param contextRefreshedEvent 上下文加载完成事件
     */
    @EventListener(classes = ContextRefreshedEvent.class)
    public void contextRefreshedEvent(ContextRefreshedEvent contextRefreshedEvent){
        String resourceManagerEndpoint = System.getenv("RESOURCE_MANAGER_ENDPOINT");
        if (StringUtils.isBlank(resourceManagerEndpoint)){
            log.info("[resourceManager],客户端初始化时,RESOURCE_MANAGER_ENDPOINT不存在,使用def值: 0.0.0.0:10400");
            resourceManagerEndpoint = "0.0.0.0:10400";
        }
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(resourceManagerEndpoint)
                .keepAliveWithoutCalls(true)
                .usePlaintext()
                .build();
        // 注入资源管理器的grpc客户端
        resourceManagerAcl.setResourceManagerStub(ResourceManagerGrpc.newBlockingStub(channel));
        shutdownHook.shutdownManagedChannelHook(channel);
    }

}
