package com.jxin.faas.scheduler.infrastructure.plug.db.type.mybatis.properties;

import lombok.Data;

import java.util.Map;

/**
 * 复数DB配置参数类
 * @author Jxin
 * @version 1.0
 * @since 2020/1/13 20:01
 */
@Data
public class MutilDbProperties {
    private Map<String, DbProperties> dbs;
}
