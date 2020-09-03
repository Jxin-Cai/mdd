package com.jxin.faas.scheduler.domain.util;

import java.util.List;

/**
 * 环境变量读取器
 * @author Jxin
 * @version 1.0
 * @since 2020/7/21 14:34
 */
public interface IEnvReader {
    /**
     * 从环境变量中抽取属性,注入实体bean
     * @param  clazz       类的字节码对象
     * @param  prefixName  前缀
     * @return 带有配置属性的实体bean
     * @author Jxin
     */
     <T> T readSetting(Class<T> clazz, String prefixName);
    /**
     * 从环境变量中抽取属性,注入实体bean,返回所有实体bean集合
     * @param  clazz       类的字节码对象
     * @param  prefixName  前缀
     * @return 带有配置属性的实体bean集合
     * @author Jxin
     */
     <T> List<T> readListSetting(Class<T> clazz, String prefixName);
}
