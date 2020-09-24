package com.jxin.faas.scheduler.domain.container.service;

import com.jxin.faas.scheduler.domain.container.dmo.val.FuncVal;

/**
 * 函数 管理服务
 * @author Jxin
 * @version 1.0
 * @since 2020/9/15 17:52
 */
public interface IFuncManager {
    /**
     * 保存函数数据 (当函数不存在时)
     * @param  funcVal 函数值对象
     */
    void saveWhenNone(FuncVal funcVal);
}
