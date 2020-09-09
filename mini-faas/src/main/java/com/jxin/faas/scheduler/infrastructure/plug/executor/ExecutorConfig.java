package com.jxin.faas.scheduler.infrastructure.plug.executor;

import com.jxin.faas.scheduler.infrastructure.util.EnvUtil;
import com.jxin.faas.scheduler.infrastructure.plug.executor.properties.ExecutorProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步线程池配置类
 * @author Jxin
 * @version 1.0
 * @since 2020/7/21 17:35
 */
@Configuration
@Slf4j
public class ExecutorConfig implements BeanFactoryPostProcessor, EnvironmentAware {
    private ConfigurableEnvironment environment;
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        final List<ExecutorProperties> executors =
                EnvUtil.readListSetting(ExecutorProperties.class, "executors", environment);
        if(CollectionUtils.isEmpty(executors)){
            return;
        }
        log.info("=======开始注册线程=======");
        for (ExecutorProperties executorProperties : executors) {
            final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(executorProperties.getCorePoolSize());
            executor.setMaxPoolSize(executorProperties.getMaxPoolSize());
            executor.setQueueCapacity(executorProperties.getQueueCapacity());
            executor.setThreadNamePrefix(executorProperties.getId());
            executor.setKeepAliveSeconds(executorProperties.getKeepAlive());
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
            executor.initialize();
            EnvUtil.register(configurableListableBeanFactory, executor, executorProperties.getId(), "");
            log.info("线程池注册成功, Id: {}", executorProperties.getId());
        }
        log.info("=======注册线程结束=======");
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }
}
