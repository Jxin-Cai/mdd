package com.jxin.faas.scheduler.domain.container.repository.persistence;

import com.jxin.faas.scheduler.domain.container.repository.table.FuncDO;

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
    void saveWhenNone(FuncDO func);

    /**
     * 获取函数 do
     * @param  name 函数名
     * @return {@link FuncDO}
     */
    Optional<FuncDO> getFunc(String name);
}
