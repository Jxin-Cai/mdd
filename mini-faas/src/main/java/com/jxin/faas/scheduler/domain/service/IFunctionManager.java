package com.jxin.faas.scheduler.domain.service;

import com.jxin.faas.scheduler.domain.entity.dmo.Function;
import com.jxin.faas.scheduler.domain.entity.val.FunctionInfoVal;

import java.util.Optional;

/**
 * 函数管理器
 * @author Jxin
 * @version 1.0
 * @since 2020/7/28 17:07
 */
public interface IFunctionManager {
    /**
     * 推送函数
     * @param  functionInfoVal 函数信息值对象
     * @return 记录方法执行成功返回 true
     */
    boolean pushFunc(FunctionInfoVal functionInfoVal);
    /**记录异常方法的异常次数*/
    void recordErrFunc(String funcName);

    /**
     * 判断是不是异常方法
     * @param  funcName 函数名
     * @return 如果是异常方法返回true
     */
    boolean isBadFunc(String funcName);
    /**
     * 获取函数
     * @param  funcName 函数名
     * @return 函数领域对象
     */
    Optional<Function> getFunction(String funcName);
}
