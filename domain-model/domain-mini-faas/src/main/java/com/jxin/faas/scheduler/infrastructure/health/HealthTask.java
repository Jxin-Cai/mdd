package com.jxin.faas.scheduler.infrastructure.health;

import cn.hutool.system.SystemUtil;
import com.jxin.faas.scheduler.infrastructure.health.entity.ThreadPoolHealth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 系统健康监控定时器
 * @author Jxin
 * @version 1.0
 * @since 2020/7/20 21:26
 */
// @Component
@Slf4j
public class HealthTask {
    private final Map<String, ThreadPoolTaskExecutor> sxecutorMap;

    @Autowired
    public HealthTask(Map<String, ThreadPoolTaskExecutor> sxecutorMap) {
        this.sxecutorMap = sxecutorMap;
    }

    /**
     * 3S扫一次
     */
    @Scheduled(initialDelay = 5000, fixedRate = 3000)
    public void testTask(){
        sxecutorMap.forEach((key, value) -> threadMonitoring(value.getThreadPoolExecutor(), key));
        log.info("[内存情况监控],总内存 : {} m, 空闲内存: {} m, 已用内存: {} m",
                 SystemUtil.getTotalMemory() / 1024 / 1024,
                 SystemUtil.getFreeMemory() / 1024 / 1024,
                 (SystemUtil.getTotalMemory() - SystemUtil.getFreeMemory()) / 1024 / 1024);
        log.info("[线程数监控],线程总数 : {}", SystemUtil.getTotalThreadCount());


    }

    /**
     * 打印线程池状态
     * @param  tpte    定时任务线程池
     * @param  tpeName 线程池名称
     * @author Jxin
     */
    private void threadMonitoring(ThreadPoolExecutor tpte, String tpeName) {
        final ThreadPoolHealth threadPoolHealth = new ThreadPoolHealth(tpte.getQueue().size(),
                                                                        tpte.getActiveCount(),
                                                                        tpte.getPoolSize(),
                                                                        tpte.getCompletedTaskCount(),
                                                                        tpte.getTaskCount());

        log.info("[线程池状态监控],线程池名称:{},健康状态:{}", tpeName, threadPoolHealth);
    }
}
