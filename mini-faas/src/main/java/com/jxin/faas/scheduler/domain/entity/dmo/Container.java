package com.jxin.faas.scheduler.domain.entity.dmo;

import cn.hutool.core.util.NumberUtil;
import com.google.common.collect.Maps;
import com.jxin.faas.scheduler.domain.entity.val.ContainerStatVal;
import lombok.Data;
import lombok.Synchronized;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

/**
 * 容器领域对象
 * @author Jxin
 * @version 1.0
 * @since 2020/7/21 14:20
 */
@Data
public class Container {
    private static final BigDecimal CPU_RATE_LIMITER_MAX = new BigDecimal(100);
    /**容器Id*/
    private String containerId;
    /**节点Id*/
    private String nodeId;
    /**节点顺序*/
    private Integer nodeOrder;
    /**节点地址*/
    private String address;
    /**节点端口*/
    private Long port;
    /**函数名*/
    private String funcName;
    /**内存大小*/
    private Long memorySize;

    /**剩余资源是否足够*/
    private volatile boolean enough = true;
    /**销毁状态*/
    private volatile boolean destroy = false;
    /**限流数量*/
    private Long rateLimiterCount;
    /**资源刷新间隔的并发限制器*/
    private volatile Semaphore rateLimiter;
    /**内存使用率*/
    private BigDecimal memUsageRatio;
    /**cpu使用率*/
    private BigDecimal cpuUsageRatio;
    /**节点cpu占比*/
    private BigDecimal nodeCpuRatio;
    /**请求任务map*/
    private Map<String, Instant> reqJobMap = Maps.newHashMap();
    /**最后执行时间*/
    private Instant lastTime;
    /**创建时间*/
    private Instant createTime;

    private Container(String containerId,
                     String nodeId,
                     String address,
                     Long port,
                     Integer nodeOrder,
                     String funcName,
                     Long memorySize,
                     BigDecimal memUsageRatio,
                     BigDecimal cpuUsageRatio,
                     BigDecimal nodeCpuRatio,
                     Instant lastTime,
                     Instant createTime) {
        this.containerId = containerId;
        this.nodeId = nodeId;
        this.address = address;
        this.port = port;
        this.nodeOrder = nodeOrder;
        this.funcName = funcName;
        this.memorySize = memorySize;
        this.memUsageRatio = memUsageRatio;
        this.cpuUsageRatio = cpuUsageRatio;
        this.nodeCpuRatio = nodeCpuRatio;
        this.lastTime = lastTime;
        this.createTime = createTime;
        if(memorySize == 0){
            rateLimiterCount = 10000L;
        }else {
            long limiter = 60 * memorySize / 3985944576L;
            rateLimiterCount = limiter == 0? 1: limiter;
        }
        rateLimiter = new Semaphore(rateLimiterCount.intValue());
    }


    /**
     * 创建容器实例
     * @param containerId  容器Id
     * @param node         节点
     * @param nodeCpuRatio 当前容器的宿主机cpu占比
     * @param funcName     函数名
     * @param memorySize   内存大小
     */
    public static Container of(String containerId, Node node, BigDecimal nodeCpuRatio, String funcName, Long memorySize){
        return new Container(containerId, node.getId(),
                             node.getAddress(),
                             node.getPort(),
                             node.getOrder(),
                             funcName,
                             memorySize,
                             new BigDecimal(0),
                             new BigDecimal(0),
                             nodeCpuRatio,
                             Instant.now(),
                             Instant.now());
    }
    //**********************************************job*****************************************************************

    /**
     * 获取运行中的请求数
     * @return 运行中的请求
     */
    public int getReqJobSize(){
        return reqJobMap.size();
    }
    /**
     * 添加执行任务
     * @param  requestId 请求Id
     * @return 添加成功返回 true
     */
    @Synchronized
    public boolean addRunJob(String requestId){
        if(!enough){
            return false;
        }
/*        if(!rateLimiter.tryAcquire()){
            return false;
        }*/
        reqJobMap.putIfAbsent(requestId, Instant.now());
        lastTime = Instant.now();
        return true;
    }

    /**
     * 删除执行任务
     */
    @Synchronized
    public void removeRunJob(String requestId){
        reqJobMap.remove(requestId);
    }

    /**
     * 判断当前容器是否可以被清理
     * @param  delayTime 延迟时间
     * @return 如果当前容器可以被清理 , 返回true
     */
    @Synchronized
    public boolean canClean(int delayTime){
        if(destroy){
            return true;
        }
        if(isEmpty() && ChronoUnit.SECONDS.between(lastTime, Instant.now()) >= delayTime){
            destroy = true;
            return true;
        }
        return false;
    }

    /**
     * 判断是否需要扩容容器
     * @param  needScaleCpuUsageRatio 需要扩容的cpu使用率
     * @param  needScaleMemUsageRatio 需要扩容的内存使用率
     * @return 如果容器需要扩容, 返回true
     */
    public boolean needScale(BigDecimal needScaleCpuUsageRatio, BigDecimal needScaleMemUsageRatio){
        if(!enough){
            return true;
        }
        return cpuUsageRatio.compareTo(needScaleCpuUsageRatio) > 0 || memUsageRatio.compareTo(needScaleMemUsageRatio) > 0;
    }

    /**
     * 判断是否需要迁移请求
     * 容器存活时间 >= minLiveTime && 容器中执行任务数 > 0
     * @param  minLiveTime 最小存活时间 (s)
     * @return 需要迁移请求则返回 true
     */
    public boolean needMoveReq(Integer minLiveTime){
        if(ChronoUnit.SECONDS.between(createTime, Instant.now() ) < minLiveTime){
            return false;
        }
        return !isEmpty();
    }
    //**********************************************resource************************************************************
    /**
     * 刷新容器资源使用情况
     * @param nodeOverload     节点超载
     * @param containerStat    容器状态信息  值对象
     * @param maxCpuUsageRatio 容器最大资源使用比例
     * @param maxMemUsageRatio 容器最大内存使用比例
     */
    @Synchronized
    public void refreshEnough(boolean nodeOverload,
                              ContainerStatVal containerStat,
                              BigDecimal maxCpuUsageRatio,
                              BigDecimal maxMemUsageRatio){

        // 清理超时任务
        cleanTtlJob(10);
        if(containerStat == null){
            return;
        }
        if(nodeOverload){
            enough = false;
            return;
        }

        final BigDecimal memoryUsageRatio;
        if(containerStat.getMemoryAllSize().equals(0L)){
            memoryUsageRatio = BigDecimal.ZERO;
        }else {
            memoryUsageRatio = NumberUtil.div(containerStat.getMemoryUsageSize(),
                                              containerStat.getMemoryAllSize());
        }
        this.memUsageRatio = memoryUsageRatio;
        this.cpuUsageRatio = NumberUtil.div(containerStat.getCpuUsageRatio(), nodeCpuRatio);
        this.cpuUsageRatio = cpuUsageRatio.compareTo(CPU_RATE_LIMITER_MAX) > 0? CPU_RATE_LIMITER_MAX : cpuUsageRatio;
        // 获取当前资源占比下,还能有多少限流指标
        final int rateLimiterSize = Math.max(1, Math.min(
                NumberUtil.mul(rateLimiterCount, NumberUtil.sub(1, memUsageRatio)).intValue(),
                NumberUtil.div(NumberUtil.mul(rateLimiterCount, NumberUtil.sub(100, cpuUsageRatio)),CPU_RATE_LIMITER_MAX).intValue())
        );
        rateLimiter = new Semaphore(rateLimiterSize);
        if(memUsageRatio.compareTo(maxMemUsageRatio) > 0){
            enough = false;
            return;
        }



        if(cpuUsageRatio.compareTo(maxCpuUsageRatio) > 0){
            enough = false;
            return;
        }

        enough = true;
    }

    //**********************************************private***********************************************************
    /**
     * @return 运行中的任务为0 时返回true
     */
    private boolean isEmpty(){
        return reqJobMap.isEmpty();
    }

    /**
     * 清理超时的任务
     * @param ttl 超时时间
     */
    private void cleanTtlJob(int ttl){
        if(reqJobMap.isEmpty()){
            return;
        }
        final List<String> needRemveReq = reqJobMap.entrySet()
                .stream()
                .filter(entry -> ChronoUnit.SECONDS.between(entry.getValue(), Instant.now()) >= ttl)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        if(needRemveReq.isEmpty()){
            return;
        }
        needRemveReq.forEach(reqJobMap::remove);
    }

}
