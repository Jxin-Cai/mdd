package com.jxin.faas.scheduler.infrastructure.util;

import java.util.List;

/**
 * json工具
 * @author Jxin
 * @version 1.0
 * @since 2020/7/24 14:19
 */
public interface IJsonUtil {
    /**
     * 从jsonStr中转换出实体
     * @param  json json字符串
     * @param  clzz 承接数据的类
     * @param  <T> 类的类型
     * @return 承接数据的实体
     */
    <T> T json2Bean(String json, Class<T> clzz);
    /**
     * 从jsonStr中转换出实体列表
     * @param  json json字符串
     * @param  clzz 承接数据的类
     * @param  <T> 类的类型
     * @return 承接数据的实体列表
     */
    <T> List<T> json2BeanList(String json, Class<T> clzz);

    /**
     * 实体转换成字符串
     * @param  t   实体
     * @param  <T> 类型
     * @return
     */
    <T> String beanJson(T t);
}
