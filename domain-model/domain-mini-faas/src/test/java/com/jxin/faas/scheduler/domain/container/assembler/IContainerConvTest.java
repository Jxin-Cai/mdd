package com.jxin.faas.scheduler.domain.container.assembler;

import com.jxin.faas.scheduler.SchedulerApplication;
import com.jxin.faas.scheduler.domain.container.dmo.entity.Container;
import com.jxin.faas.scheduler.domain.container.dmo.val.FuncVal;
import com.jxin.faas.scheduler.domain.container.dmo.val.NodeVal;
import com.jxin.faas.scheduler.domain.container.repository.table.ContainerDO;
import com.jxin.faas.scheduler.domain.container.repository.table.NodeDO;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import schedulerproto.AcquireContainerReply;

/**
 * @author Jxin
 * @version 1.0
 * @since 2020/9/22 19:02
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SchedulerApplication.class)
class IContainerConvTest {
    @Autowired
    private IContainerConv containerConv;
    @Test
    void domain2Dto() {
        final NodeDO nodeDO = new NodeDO();
        nodeDO.setNodeId("aa");
        nodeDO.setAddress("aa");
        nodeDO.setPort(0);

        final NodeVal nodeVal = NodeVal.of(nodeDO);
        final FuncVal funcVal = new FuncVal();
        funcVal.setName("aa");

        final Container container = Container.of("test", nodeVal, funcVal);
        final ContainerDO containerDO = containerConv.domain2Do(container);
        Assert.assertNull(containerDO);
        final AcquireContainerReply acquireContainerReply = containerConv.domain2Dto(container);
        Assert.assertNull(acquireContainerReply);
    }
}