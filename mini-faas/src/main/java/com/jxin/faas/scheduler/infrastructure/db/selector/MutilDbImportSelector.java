package com.jxin.faas.scheduler.infrastructure.db.selector;

import com.jxin.faas.scheduler.infrastructure.db.annotation.EnableMutilDb;
import com.jxin.faas.scheduler.infrastructure.db.consts.PersistentTypeEnum;
import com.jxin.faas.scheduler.infrastructure.db.type.mybatis.configuration.MybatisMutilDataSourceConfiguration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * MutilDb 模块的 启动选择器
 * @author Jxin
 * @version 1.0
 * @since 2020/1/14 20:10
 */
public class MutilDbImportSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        // 读取 EnableMutilDb 中的所有的属性方法
       final Map<String, Object> annotationAttributes =
               annotationMetadata.getAnnotationAttributes(EnableMutilDb.class.getName());
        // 获取名为"type" 的属相方法,并且强制转化成 TypeEnum 类型
        final PersistentTypeEnum type = (PersistentTypeEnum) annotationAttributes.get("type");

        switch (type) {
            case MYBATIS:
                return new String[]{MybatisMutilDataSourceConfiguration.class.getName()};
                // HIBERNATE 的就不写了
            case HIBERNATE:
                return new String[]{MybatisMutilDataSourceConfiguration.class.getName()};
            default:
                throw new IllegalArgumentException("非法的 持久层框架类型");
        }
    }
}
