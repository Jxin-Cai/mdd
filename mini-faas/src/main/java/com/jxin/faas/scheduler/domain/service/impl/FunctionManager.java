package com.jxin.faas.scheduler.domain.service.impl;

import com.google.common.collect.Maps;
import com.jxin.faas.scheduler.domain.entity.dmo.Function;
import com.jxin.faas.scheduler.domain.entity.val.FunctionInfoVal;
import com.jxin.faas.scheduler.domain.service.IFunctionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * 函数管理器
 * @author Jxin
 * @version 1.0
 * @since 2020/7/28 17:09
 */
@Service
public class FunctionManager implements IFunctionManager {
    @Value("${functionManager.awaitTime}")
    private int awaitTime;
    @Value("${functionManager.descentIntervalTime}")
    private int descentIntervalTime;
    /**
     * funcName : Function
     */
    private static final Map<String, Function> FUNC_MAP = Maps.newConcurrentMap();
    @Override
    public boolean pushFunc(FunctionInfoVal functionInfoVal) {
        final Function function = FUNC_MAP.computeIfAbsent(functionInfoVal.getName(),
                                    o -> Function.of(awaitTime, descentIntervalTime, functionInfoVal));
        return function.refreshLastTime();
    }

    @Override
    public void recordErrFunc(String funcName) {
        FUNC_MAP.get(funcName).addErrCount();
    }

    @Override
    public boolean isBadFunc(String funcName) {
        return FUNC_MAP.get(funcName).isBadFunc();
    }

    @Override
    public Optional<Function> getFunction(String funcName) {
        return Optional.of(FUNC_MAP.get(funcName));
    }
}
