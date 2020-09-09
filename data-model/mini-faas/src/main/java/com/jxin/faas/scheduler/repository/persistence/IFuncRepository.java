package com.jxin.faas.scheduler.repository.persistence;

import com.jxin.faas.scheduler.repository.table.Func;

import java.util.Optional;

/**
 * 函数持久层接口
 * @author Jxin
 * @version 1.0
 * @since 2020/9/3 19:51
 */
public interface IFuncRepository {
    /**
     * 保存函数数据 (当函数不存在时)
     * @param  func 函数 do
     */
    void saveWhenNone(Func func);

    /**
     * 获取函数 do
     * @param  name 函数名
     * @return {@link Func}
     */
    Optional<Func> getFunc(String name);
}
