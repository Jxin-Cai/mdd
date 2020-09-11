package com.jxin.faas.scheduler.infrastructure.util.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jxin.faas.scheduler.infrastructure.util.IJsonUtil;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * gson实现的json工具
 * @author Jxin
 * @version 1.0
 * @since 2020/7/24 14:29
 */
@Component
public class GsonJsonUtil implements IJsonUtil {
    private static final ThreadLocal<Gson> THREAD_LOCAL = ThreadLocal.withInitial(Gson::new);
    @Override
    public <T> T json2Bean(String json, Class<T> clzz) {
        return THREAD_LOCAL.get().fromJson(json, clzz);
    }

    @Override
    public <T> List<T> json2BeanList(String json, Class<T> clzz) {
        return THREAD_LOCAL.get().fromJson(json, new TypeToken<List<T>>(){}.getType());
    }

    @Override
    public <T> String beanJson(T t) {
        return THREAD_LOCAL.get().toJson(t);
    }
}
