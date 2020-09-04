package com.jxin.faas.scheduler.infrastructure.db.consts;

import com.jxin.faas.scheduler.infrastructure.db.type.mybatis.properties.JdbcPoolConfigProperties;

import java.util.function.Supplier;

/**
 * HikariCp的默认配置参数常量
 * @author Jxin
 * @version 1.0
 * @since 2020/1/13 20:01
 */
public interface HikariCpDefaultConfigConsts {
    /**
     * 连接池初始化对象
     */
    Supplier<JdbcPoolConfigProperties> DEFAULT_POOL = JdbcPoolConfigProperties::new;

    /**
     * log4jdbc插件的配置,因为是补充hikaricp日志上的缺陷,所以放在一起
     */
    String[] LOG4JDBC_PROPERTIES_TO_COPY = {
            "log4jdbc.debug.stack.prefix",
            "log4jdbc.sqltiming.warn.threshold",
            "log4jdbc.sqltiming.error.threshold",
            "log4jdbc.dump.booleanastruefalse",
            "log4jdbc.dump.fulldebugstacktrace",
            "log4jdbc.dump.sql.maxlinelength",
            "log4jdbc.statement.warn",
            "log4jdbc.dump.sql.select",
            "log4jdbc.dump.sql.insert",
            "log4jdbc.dump.sql.update",
            "log4jdbc.dump.sql.delete",
            "log4jdbc.dump.sql.create",
            "log4jdbc.dump.sql.addsemicolon",
            "log4jdbc.auto.load.popular.drivers",
            "log4jdbc.drivers",
            "log4jdbc.trim.sql",
            "log4jdbc.trim.sql.extrablanklines",
    };

    /**
     * 此属性控制从池返回的连接的默认自动提交行为。它是一个布尔值。
     * 默认值：true
     */
    boolean DEFAULT_AUTO_COMMIT = true;


    /**
     * 此属性控制客户端（即您）将等待来自池的连接的最大毫秒数。
     * 如果在没有可用连接的情况下超过此时间,则会抛出SQLException。
     * 最低可接受的连接超时时间为250 ms。
     * 默认值：30000（30秒）
     */
    int DEFAULT_CONNECTION_TIMEOUT = 60000;


    /**
     * 此属性控制允许连接在池中闲置的最长时间。
     * 此设置仅适用于minimumIdle定义为小于maximumPoolSize。
     * 一旦池达到连接,空闲连接将不会退出minimumIdle。
     * 连接是否因闲置而退出,最大变化量为+30秒,平均变化量为+15秒。在超时之前,连接永远不会退出。
     * 值为0意味着空闲连接永远不会从池中删除。允许的最小值是10000ms（10秒）。
     * 默认值：600000（10分钟）
     */
    int DEFAULT_IDLE_TIMEOUT = 600000;


    /**
     * 此属性控制池中连接的最大生存期。正在使用的连接永远不会退休,只有在关闭后才会被删除。
     * 在逐个连接的基础上,应用较小的负面衰减来避免池中的大量消失。
     * 我们强烈建议设置此值,并且应该比任何数据库或基础设施规定的连接时间限制短几秒。
     * 值为0表示没有最大寿命（无限寿命）,当然是idleTimeout设定的主题。
     * 默认值：1800000（30分钟）
     */
    int DEFAULT_MAX_LIFETIME = 1800000;


    /**
     * 此属性控制池允许达到的最大大小,包括空闲和正在使用的连接。
     * 基本上这个值将决定到数据库后端的最大实际连接数。对此的合理价值最好由您的执行环境决定。
     * 当池达到此大小并且没有空闲连接可用时,对getConnection（）的调用将connectionTimeout在超时前阻塞达几毫秒。
     * 请阅读关于连接池尺寸。
     * 默认值：10
     */
    int DEFAULT_MAXIMUM_POOLSIZE = 1000;


    /**
     * 该属性控制HikariCP尝试在池中维护的最小空闲连接数。
     * 如果空闲连接低于此值并且连接池中的总连接数少于此值maximumPoolSize,则HikariCP将尽最大努力快速高效地添加其他连接。
     * 但是,为了获得最佳性能和响应尖峰需求,我们建议不要设置此值,而是允许HikariCP充当固定大小的连接池。
     * 默认值：与maximumPoolSize相同
     */
    int DEFAULT_MINIMUM_IDLE = DEFAULT_MAXIMUM_POOLSIZE;

    /**
     * 如果池无法成功初始化连接,则此属性控制池是否将“快速失败”。
     * 任何正数都取为尝试获取初始连接的毫秒数; 应用程序线程将在此期间被阻止。
     * 如果在超时发生之前无法获取连接,则会引发异常。
     * 此超时被应用后的connectionTimeout 期。
     * 如果值为零（0）,HikariCP将尝试获取并验证连接。如果获得连接但未通过验证,将抛出异常并且池未启动。
     * 但是,如果无法获得连接,则会启动该池,但后续获取连接的操作可能会失败。
     * 小于零的值将绕过任何初始连接尝试,并且在尝试获取后台连接时,池将立即启动。
     * 因此,以后努力获得连接可能会失败。
     * 默认值：1
     */
    int DEFAULT_INITIALIZATION_FAIL_TIMEOUT = 1;


    /**
     * 如果池无法成功初始化连接,则此属性控制池是否将“快速失败”。
     * 任何正数都取为尝试获取初始连接的毫秒数; 应用程序线程将在此期间被阻止。
     * 如果在超时发生之前无法获取连接,则会引发异常。
     * 此超时被应用后的connectionTimeout 期。如果值为零（0）,HikariCP将尝试获取并验证连接。
     * 如果获得连接但未通过验证,将抛出异常并且池未启动。
     * 但是,如果无法获得连接,则会启动该池,但后续获取连接的操作可能会失败。
     * 小于零的值将绕过任何初始连接尝试,并且在尝试获取后台连接时,池将立即启动。因此,以后努力获得连接可能会失败。
     * 默认值：1
     */
    boolean DEFAULT_ISOLATE_INTERNAL_QUERIES = false;

    /**
     * 该属性控制池是否可以通过JMX暂停和恢复。这对于某些故障转移自动化方案很有用。
     * 当池被暂停时,呼叫 getConnection()将不会超时,并将一直保持到池恢复为止。
     * 默认值：false
     */
    boolean DEFAULT_ALLOW_POOL_SUSPENSION = false;

    /**
     * 此属性控制默认情况下从池中获取的连接是否处于只读模式。
     * 注意某些数据库不支持只读模式的概念,而其他数据库则在Connection设置为只读时提供查询优化。
     * 无论您是否需要此属性,都将主要取决于您的应用程序和数据库。
     * 默认值：false
     */
    boolean DEFAULT_READ_ONLY = false;

    /**
     * 该属性控制是否注册JMX管理Bean（“MBeans”）。
     * 默认值：false
     */
    boolean DEFAULT_REGISTER_MBEANS = false;

    /**
     * HikariCP将尝试通过DriverManager仅基于驱动程序来解析驱动程序jdbcUrl,
     * 但对于一些较旧的驱动程序,driverClassName还必须指定它。
     * 除非您收到明显的错误消息,指出找不到驱动程序,否则请忽略此属性。
     * 默认值：无
     */
    String DEFAULT_DRIVER_CLASS_NAME = null;

    /**
     * 此属性控制连接测试活动的最长时间。这个值必须小于connectionTimeout。最低可接受的验证超时时间为250 ms。
     * 默认值：5000
     */
    int DEFAULT_VALIDATION_TIMEOUT = 5000;

    /**
     * 此属性控制在记录消息之前连接可能离开池的时间量,表明可能存在连接泄漏。值为0意味着泄漏检测被禁用。
     * 启用泄漏检测的最低可接受值为2000（2秒）。
     * 默认值：0
     */
    int DEFAULT_LEAK_DETECTION_THRESHOLD = 0;
}
