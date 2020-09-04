package com.jxin.faas.scheduler;

import com.jxin.faas.scheduler.infrastructure.db.annotation.EnableMutilDb;
import com.jxin.faas.scheduler.infrastructure.db.consts.PersistentTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动类
 * @author Jxin
 * @version 1.0
 * @since 2020/7/20 18:57
 */

@EnableScheduling
@EnableAsync
@SpringBootApplication
// @SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
@EnableMutilDb(type = PersistentTypeEnum.MYBATIS)
@Slf4j
public class SchedulerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplication.class, args);
    }
}
