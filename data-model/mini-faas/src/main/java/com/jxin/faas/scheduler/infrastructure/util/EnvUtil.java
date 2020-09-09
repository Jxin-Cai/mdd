package com.jxin.faas.scheduler.infrastructure.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;


/**
 * 环境变量获取工具类
 * @author Jxin
 * @version 1.0
 * @since 2020/1/13 20:01
 */
public interface EnvUtil {
    /**
     * 从环境变量中抽取属性,注入实体bean
     * @param  clazz       类的字节码对象
     * @param  prefixName  前缀
     * @param  environment 环境变量
     * @return 带有配置属性的实体bean
     * @author Jxin
     */
    static <T> T readSetting(Class<T> clazz,
                             String prefixName,
                             ConfigurableEnvironment environment) {
        return Binder.get(environment).bind(prefixName, Bindable.of(clazz))
                .orElse(null);
    }
    /**
     * 从环境变量中抽取属性,注入实体bean,返回所有实体bean集合
     * @param  clazz       类的字节码对象
     * @param  prefixName  前缀
     * @param  environment 环境变量
     * @return 带有配置属性的实体bean集合
     * @author Jxin
     */
    static <T> List<T> readListSetting(Class<T> clazz,
                                       String prefixName,
                                       ConfigurableEnvironment environment) {
        return Binder.get(environment).bind(prefixName, Bindable.listOf(clazz))
                .orElse(null);
    }
    /**
     *  手写spring的bean注册器
     * @param  beanFactory 对象工厂
     * @param  bean        要注册的对象
     * @param  name        对象名字
     * @param  alias       对象别名
     * @author Jxin
     */
    static void register(ConfigurableListableBeanFactory beanFactory,
                         Object bean,
                         String name,
                         String alias) {
        beanFactory.registerSingleton(name, bean);
        if(StringUtils.isEmpty(alias)){
            return;
        }
        if (!beanFactory.containsSingleton(alias)) {
            beanFactory.registerAlias(name, alias);
        }
    }
}
