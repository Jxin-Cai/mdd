package com.jxin.faas.scheduler.domain.container.service.impl;

import com.jxin.faas.scheduler.domain.container.dmo.val.FuncVal;
import com.jxin.faas.scheduler.domain.container.repository.persistence.IFuncRepository;
import com.jxin.faas.scheduler.domain.container.repository.table.FuncDO;
import com.jxin.faas.scheduler.domain.container.service.IFuncManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 函数 管理服务
 * @author Jxin
 * @version 1.0
 * @since 2020/9/15 17:53
 */
@Service
public class FuncManager implements IFuncManager {
    private final IFuncRepository funcRepository;

    @Autowired
    public FuncManager(IFuncRepository funcRepository) {
        this.funcRepository = funcRepository;
    }

    @Override
    public void saveWhenNone(FuncVal funcVal) {
        // 保存函数数据 (当函数不存在时)
        funcRepository.saveWhenNone(FuncDO.of(funcVal));
    }
}
