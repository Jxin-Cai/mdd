package com.jxin.faas.scheduler.domain.util;

import cn.hutool.core.lang.UUID;

/**
 * id工具
 * @author Jxin
 * @version 1.0
 * @since 2020/7/28 19:55
 */
public interface IdUtil {

    /**
     * 生成流水请求id
     * @return 请求id
     */
    static String getRequestId(){
        return UUID.fastUUID().toString();
    }
}
