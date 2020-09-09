package com.jxin.faas.scheduler.infrastructure.plug.db.type.mybatis.configuration;

import com.google.common.base.Strings;
import com.jxin.faas.scheduler.infrastructure.plug.db.consts.HikariCpDefaultConfigConsts;
import com.jxin.faas.scheduler.infrastructure.plug.db.type.mybatis.properties.DbProperties;
import com.jxin.faas.scheduler.infrastructure.plug.db.type.mybatis.properties.MutilDbProperties;
import com.jxin.faas.scheduler.infrastructure.plug.db.type.mybatis.properties.MybatisProperties;
import com.jxin.faas.scheduler.infrastructure.util.EnvUtil;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import net.sf.log4jdbc.log.slf4j.Slf4jSpyLogDelegator;
import net.sf.log4jdbc.sql.jdbcapi.DataSourceSpy;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.lang.Nullable;
import org.springframework.transaction.aspectj.AnnotationTransactionAspect;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Optional;

/**
 * 复数数据源配置类
 * @author Jxin
 * @version 1.0
 * @since 2020/1/13 20:01
 */
@Slf4j
@ConditionalOnClass({DataSource.class, SqlSessionFactory.class, SqlSessionFactoryBean.class})
public class MybatisMutilDataSourceConfiguration implements BeanFactoryPostProcessor, EnvironmentAware {
    private ConfigurableEnvironment environment;

    @Override
    public void postProcessBeanFactory(@Nullable ConfigurableListableBeanFactory beanFactory) throws BeansException {
        Assert.notNull(beanFactory, "non null beanFactory");
        final MutilDbProperties mutilDBProperties = EnvUtil.readSetting(MutilDbProperties.class,
                                                                        StringUtils.EMPTY,
                                                                        getEnvironment());
       if(mutilDBProperties == null || MapUtils.isEmpty(mutilDBProperties.getDbs())){
           log.info("[db数据源初始化],该服务未配置数据源");
           return;
       }
        // 数据库日志类先初始化
        initLog4Jdbc();

        // 生成所有数据源的连接池并绑定上对应的mabits会话
        mutilDBProperties.getDbs().forEach((name, properties) -> createDataSource(beanFactory, name, properties));
    }

    @Override
    public void setEnvironment(@Nullable Environment environment) {
        Assert.notNull(environment, "non null environment");
        this.environment = (ConfigurableEnvironment) environment;
    }

    /**
     * 创建数据源
     * @param  beanFactory bean工厂
     * @param  name        数据库名称
     * @param  properties  数据源配置
     * @author Jxin
     */
    private void createDataSource(ConfigurableListableBeanFactory beanFactory,
                                  String name,
                                  DbProperties properties) {
        // 获得cp连接池数据源
        final HikariDataSource hikariDataSource = getHikariDataSource(properties);
        // 为cp连接池做日志代理
        final DataSource dataSource = new DataSourceSpy(hikariDataSource);
        // 获得该数据源的事物
        final DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
        // 开启注解事物管理
        AnnotationTransactionAspect.aspectOf().setTransactionManager(dataSourceTransactionManager);

        // 注册数据源
        EnvUtil.register(beanFactory, dataSource, name + "DataSource", name + "DS");
        log.info("{}数据源注册成功", name);
        // 注册事物托管对象
        EnvUtil.register(beanFactory,
                                 dataSourceTransactionManager,
                          name + "DataSourceTransactionManager", name + "TM");
        log.info("{}事物管理对象注册成功", name);
        // 注册sql回话工厂
        EnvUtil.register(beanFactory,
                                 createSqlSessionFactory(properties.getMybatis(), dataSource),
                                name + "sqlSessionFactory", name + "SF");
        log.info("{}sql回话工厂对象注册成功", name);
        // 注册mybatis的dao扫描器
        if (!Strings.isNullOrEmpty(properties.getMybatis().getMapperScanner())) {
            registerBasePackageScanner((BeanDefinitionRegistry) beanFactory,
                                       properties.getMybatis().getMapperScanner(),
                                       name);
            log.info("{}的dao接口绑定扫描器对象注册成功", name);
        }
    }


    /**
     * 初始化log4jdbc的配置
     * @author Jxin
     */
    private void initLog4Jdbc() {
        Arrays.stream(HikariCpDefaultConfigConsts.LOG4JDBC_PROPERTIES_TO_COPY)
                .filter(getEnvironment() :: containsProperty)
                .forEach(property -> {
                    final String value = getEnvironment().getProperty(property);
                    if(StringUtils.isNotBlank(value)){
                        System.setProperty(property, value);
                    }
                });

        System.setProperty("log4jdbc.spylogdelegator.name",
                           this.environment.getProperty("log4jdbc.spylogdelegator.name",
                           Slf4jSpyLogDelegator.class.getName()));
    }

    /**
     * 创建会话工厂
     * @param  mybatisProperties mybatis配置
     * @param  dataSource        数据源
     * @return 该数据源的mybatis会话工厂
     * @author Jxin
     */
    private @Nullable
    SqlSessionFactory createSqlSessionFactory(MybatisProperties mybatisProperties,
                                              DataSource dataSource) {
        SqlSessionFactory sqlSessionFactory = null;
        try {
            final SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
            sqlSessionFactoryBean.setDataSource(dataSource);
            // 列出的所有地址只要有一个不为null或者空就拿数据
            if (!Strings.isNullOrEmpty(mybatisProperties.getMapperLocations())) {
                sqlSessionFactoryBean.setMapperLocations(mybatisProperties.resolveMapperLocations());
            }

            Optional.ofNullable(mybatisProperties.getAliasPackage())
                    .ifPresent(sqlSessionFactoryBean::setTypeAliasesPackage);
            // mybatis配置文件的路径
            sqlSessionFactoryBean.setConfigLocation(new ClassPathResource(mybatisProperties.getConfigLocation()));
            sqlSessionFactory = sqlSessionFactoryBean.getObject();

            if (sqlSessionFactory == null) {
                log.error("sql回话工厂对象 is null");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return sqlSessionFactory;


    }

    /**
     * dao接口绑定扫描器注册
     * @param registry    注册起
     * @param basePackage 包路径
     * @param name        数据库名称
     */
    private void registerBasePackageScanner(BeanDefinitionRegistry registry,
                                            String basePackage,
                                            String name) {
        final MapperScannerConfigurer scannerConfigurer = new MapperScannerConfigurer();
        scannerConfigurer.setBasePackage(basePackage);
        scannerConfigurer.setSqlSessionFactoryBeanName(name + "SF");
        scannerConfigurer.postProcessBeanDefinitionRegistry(registry);
    }

    /**
     * 获得cp连接池数据源
     * @param  properties 配置参数
     * @return cp连接池数据源
     * @author Jxin
     */
    private HikariDataSource getHikariDataSource(DbProperties properties) {
        final HikariDataSource result = new HikariDataSource();
        result.setJdbcUrl(properties.getUrl());
        result.setUsername(properties.getUsername());
        result.setPassword(properties.getPassword());
        result.setAutoCommit(properties.getPool().isAutoCommit());
        result.setConnectionTimeout(properties.getPool().getConnectionTimeout());
        result.setIdleTimeout(properties.getPool().getIdleTimeout());
        result.setMaxLifetime(properties.getPool().getMaxLifetime());
        result.setMaximumPoolSize(properties.getPool().getMaximumPoolSize());
        result.setMinimumIdle(properties.getPool().getMinimumIdle());
        result.setInitializationFailTimeout(properties.getPool().getInitializationFailTimeout());
        result.setIsolateInternalQueries(properties.getPool().isIsolateInternalQueries());
        result.setAllowPoolSuspension(properties.getPool().isAllowPoolSuspension());
        result.setReadOnly(properties.getPool().isReadOnly());
        result.setDriverClassName(properties.getPool().getDriverClassName());
        result.setRegisterMbeans(properties.getPool().isRegisterMbeans());
        result.setValidationTimeout(properties.getPool().getValidationTimeout());
        result.setLeakDetectionThreshold(properties.getPool().getLeakDetectionThreshold());
        return result;
    }

    public ConfigurableEnvironment getEnvironment() {
        return environment;
    }

}
