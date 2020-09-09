package com.jxin.faas.scheduler.infrastructure.plug.db.type.mybatis.properties;

import com.jxin.faas.scheduler.infrastructure.plug.db.consts.HikariCpDefaultConfigConsts;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * DB配置参数类
 * @author Jxin
 * @version 1.0
 * @since 2020/1/13 20:01
 */
@Data
public class DbProperties {
    @NotNull(message = "username必须配置")
    private String username;
    @NotNull(message = "password必须配置")
    private String password;
    @NotNull(message = "url必须配置")
    private String url;
    private String driverClassName;
    /**cp连接池配置参数*/
    private JdbcPoolConfigProperties pool = HikariCpDefaultConfigConsts.DEFAULT_POOL.get();
    /**mysql的配置参数*/
    private MybatisProperties mybatis;
}
