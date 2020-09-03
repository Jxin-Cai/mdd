package com.jxin.faas.scheduler.domain.util;

/**
 * 上下文 bean工具
 * @author Jxin
 * @version 1.0
 * @since 2020/7/22 20:27
 */
public interface IContextBeanUtil {
    /**
     * 根据实例名称和类 获取实例
     * @param  name  类的实例名称
     * @param  clazz 类的字节码
     * @param  <T>   类的泛型
     * @return 实例
     * @author Jxin
     */
    <T> T getBean(String name, Class<T> clazz);
    /**
     * 根据类 获取实例
     * @param  clazz 类的字节码
     * @param  <T>   类的泛型
     * @return 实例
     * @author Jxin
     */
    <T> T getBean(Class<T> clazz);
}
