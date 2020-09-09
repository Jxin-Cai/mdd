package com.jxin.faas.scheduler.infrastructure.plug.db.annotation;

import com.jxin.faas.scheduler.infrastructure.plug.db.consts.PersistentTypeEnum;
import com.jxin.faas.scheduler.infrastructure.plug.db.selector.MutilDbImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableMutilDb 模块激活模式 Annotation
 * @author Jxin
 * @version 1.0
 * @since 2020/1/14 20:05
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MutilDbImportSelector.class)
public @interface EnableMutilDb {
    /**
     * 设置持久层框架类型
     * @return non-null
     */
    PersistentTypeEnum type() default PersistentTypeEnum.MYBATIS;
}
