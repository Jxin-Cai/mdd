package com.jxin.faas.scheduler.infrastructure.util;

import com.jxin.faas.scheduler.domain.util.IContextBeanUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * spring 上下文 bean工具
 * @author Jxin
 * @version 1.0
 * @since 2020/7/22 20:29
 */
@Component
public class SpringContextBeanUtil implements IContextBeanUtil, ApplicationContextAware {
    /**spring 上下文*/
    private ApplicationContext applicationContext;
    @Override
    public <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
