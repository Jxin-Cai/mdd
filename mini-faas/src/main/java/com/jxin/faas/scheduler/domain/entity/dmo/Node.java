package com.jxin.faas.scheduler.domain.entity.dmo;

import cn.hutool.core.util.NumberUtil;
import com.jxin.faas.scheduler.domain.entity.val.ContainerStatVal;
import com.jxin.faas.scheduler.domain.entity.val.NodeStatVal;
import lombok.Data;
import lombok.Synchronized;
import lombok.ToString;
import resourcemanagerproto.NodeDesc;

import java.math.BigDecimal;
import java.util.concurrent.Semaphore;

/**
 * 节点领域对象
 * @author Jxin
 * @version 1.0
 * @since 2020/7/21 15:47
 */
@Data
@ToString
public class Node {
    /**节点Id*/
    private String id;
    /**节点地址*/
    private String address;
    /**节点端口*/
    private Long port;
    /**节点申请的顺序, 越小越靠前*/
    private Integer order;

    /**节点空闲内存大小*/
    private Long idleMemSize;
    /**节点总的内存大小*/
    private Long totalMemSize;
    /**cpu使用占比*/
    private BigDecimal cpuUsageRatio;

    /**剩余资源是否足够*/
    private volatile boolean enough;
    /**清理中*/
    private volatile boolean cleaning;
    /**并行创建容器的限流器*/
    private Semaphore rateLimiter = new Semaphore(3);

    public Node() {
    }

    public Node(String id,
                String address,
                Long port,
                Integer order,
                Long idleMemSize,
                Long totalMemSize,
                BigDecimal cpuUsageRatio,
                boolean enough,
                boolean cleaning) {
        this.id = id;
        this.address = address;
        this.port = port;
        this.order = order;
        this.idleMemSize = idleMemSize;
        this.totalMemSize = totalMemSize;
        this.cpuUsageRatio = cpuUsageRatio;
        this.enough = enough;
        this.cleaning = cleaning;
    }


    /**
     * 初始化 节点领域对象
     * @param nodeDesc 节点信息 dto
     * @param order    节点创建顺序
     */
    public static Node of(NodeDesc nodeDesc, Integer order){
        return new Node(nodeDesc.getId(),
                        nodeDesc.getAddress(),
                        nodeDesc.getNodeServicePort(),
                        order,
                        nodeDesc.getMemoryInBytes(),
                        nodeDesc.getMemoryInBytes(),
                        new BigDecimal(0),
                        true,
                        false);
    }


    @Synchronized
    public boolean reduceMem(Long funMemorySize){
        if(!cleaning && enough && idleMemSize >= funMemorySize && rateLimiter.tryAcquire()){
            idleMemSize -= funMemorySize;
            return true;
        }
        return false;
    }

    /**
     * 归还限流次数
     */
    public void release(){
        rateLimiter.release();
    }
    /**
     * 判断是否需要扩容容器
     * @param  needScaleCpuUsageRatio 需要扩容的cpu使用率
     * @param  needScaleMemUsageRatio 需要扩容的内存使用率
     * @return 如果容器需要扩容, 返回true
     */
    public boolean needScale(BigDecimal needScaleCpuUsageRatio, BigDecimal needScaleMemUsageRatio){
        final BigDecimal memUsageRatio = NumberUtil.div(NumberUtil.sub(totalMemSize, idleMemSize), totalMemSize);
        return cpuUsageRatio.compareTo(needScaleCpuUsageRatio) > 0 || memUsageRatio.compareTo(needScaleMemUsageRatio) > 0;
    }


    //***************************************resource*******************************************************************
    /**
     * 刷新容器资源使用情况
     * @param nodeStatVal      节点状态信息 值对象
     * @param maxCpuUsageRatio 节点最大CPU资源使用比例
     * @param maxMemUsageRatio 节点最大内存资源使用比例
     */
    @Synchronized
    public void refreshEnough(NodeStatVal nodeStatVal,
                              BigDecimal maxCpuUsageRatio,
                              BigDecimal maxMemUsageRatio){
        this.idleMemSize = nodeStatVal.getMemoryAllSize() - nodeStatVal.getContainerStatList()
                                                                       .stream()
                                                                       .mapToLong(ContainerStatVal::getMemoryAllSize)
                                                                       .sum();
        this.idleMemSize = this.idleMemSize / 3 * 2;
        this.totalMemSize = nodeStatVal.getMemoryAllSize();
        this.cpuUsageRatio = nodeStatVal.getCpuUsageRatio();
        if(idleMemSize < 0){
            enough = false;
            return;
        }

        // node的总内存不可能为0
        final BigDecimal memoryUsageRatio =
                NumberUtil.div(NumberUtil.sub(totalMemSize, idleMemSize), nodeStatVal.getMemoryAllSize());
        if(memoryUsageRatio.compareTo(maxMemUsageRatio) > 0){
            enough = false;
            return;
        }
        if(cpuUsageRatio.compareTo(maxCpuUsageRatio) > 0){
            enough = false;
            return;
        }
        enough = true;
    }


}
