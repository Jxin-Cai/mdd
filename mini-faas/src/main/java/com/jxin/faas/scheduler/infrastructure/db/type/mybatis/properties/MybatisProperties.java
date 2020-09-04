package com.jxin.faas.scheduler.infrastructure.db.type.mybatis.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.io.IOException;

/**
 * mybatis的配置参数类
 * @author Jxin
 * @version 1.0
 * @since 2020/1/13 20:01
 */
@Data
@Validated
@NoArgsConstructor
@AllArgsConstructor
public class MybatisProperties {
    private static final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
    @NotNull(message = "mybatis的sqlMapConfig.xml必须配置")
    private String configLocation;
    @NotNull(message = "mybatis的mapper地址必须配置")
    private String mapperLocations;

    private String mapperScanner;

    private String aliasPackage;

    @NestedConfigurationProperty
    private Configuration configuration;


    public Resource[] resolveMapperLocations() {
        try {
            return resourceResolver.getResources(this.mapperLocations);
        } catch (IOException e) {
            return new Resource[0];
        }
    }
}
