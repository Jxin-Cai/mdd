package com.jxin.faas.scheduler.application.service.plan;

import com.jxin.faas.scheduler.domain.entity.dmo.Container;
import com.jxin.faas.scheduler.domain.entity.val.FunctionInfoVal;

import java.util.Optional;

/**
 *
 * 复用已有资源获取容器的 策略
 * @author Jxin
 * @version 1.0
 * @since 2020/8/1 11:29 下午
 */
public interface IReuseResourceGetContainerPlan {
    /**
     * 利用已有资源获取函数容器
     * @param  requestId       请求Id
     * @param  functionInfoVal 函数信息值对象
     * @return 容器
     */
    Optional<Container> reuseResourceGetContainer(String requestId, FunctionInfoVal functionInfoVal);
}
