package com.jxin.faas.scheduler.interfaces.grpc;

import cn.hutool.core.util.StrUtil;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import schedulerproto.AcquireContainerReply;
import schedulerproto.AcquireContainerRequest;
import schedulerproto.FunctionConfig;
import schedulerproto.SchedulerGrpc;

/**
 * SchedulerRpc 调用测试类
 * @author Jxin
 * @version 1.0
 * @since 2020/7/23 11:08
 */
@RunWith(SpringRunner.class)
public class SchedulerRpcTest {

    @Test
    public void acquireContainer() {
        final SchedulerGrpc.SchedulerBlockingStub schedulerBlockingStub = getSchedulerBlockingStub();
        final AcquireContainerRequest request = getRequest();
        final AcquireContainerReply acquireContainerReply = schedulerBlockingStub.acquireContainer(request);
        System.out.println(acquireContainerReply);
    }

    private AcquireContainerRequest getRequest() {
        final FunctionConfig aaaa = FunctionConfig.newBuilder().setMemoryInBytes(1024).setTimeoutInMs(10).setHandler("aaaa").build();
        return AcquireContainerRequest.newBuilder()
                .setFunctionName("test")
                .setAccountId("001")
                .setRequestId("a")
                .setFunctionConfig(aaaa)
                .build();
    }


    @Test
    public void returnContainer() {
    }

    private SchedulerGrpc.SchedulerBlockingStub getSchedulerBlockingStub() {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(StrUtil.format("{}:{}", "static://0.0.0.0", 10600))
                .usePlaintext()
                .build();
        return SchedulerGrpc.newBlockingStub(channel);
    }
}