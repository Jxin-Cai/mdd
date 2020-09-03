package com.jxin.faas.scheduler.infrastructure.env;

import com.jxin.faas.scheduler.domain.util.IEnvReader;
import com.jxin.faas.scheduler.infrastructure.util.EnvUtil;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * spring环境变量读取器
 * @author Jxin
 * @version 1.0
 * @since 2020/7/21 14:35
 */
@Component
public class SpringEnvReader implements IEnvReader, EnvironmentAware {
    private ConfigurableEnvironment environment;
    @Override
    public <T> T readSetting(Class<T> clazz, String prefixName) {
        return EnvUtil.readSetting(clazz, prefixName, environment);
    }

    @Override
    public <T> List<T> readListSetting(Class<T> clazz, String prefixName) {
        return EnvUtil.readListSetting(clazz, prefixName, environment);
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment)environment;
    }
}
