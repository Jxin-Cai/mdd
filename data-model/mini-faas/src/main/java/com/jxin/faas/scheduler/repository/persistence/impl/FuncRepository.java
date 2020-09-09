package com.jxin.faas.scheduler.repository.persistence.impl;

import com.jxin.faas.scheduler.repository.mapper.FuncMapper;
import com.jxin.faas.scheduler.repository.persistence.IFuncRepository;
import com.jxin.faas.scheduler.repository.table.Func;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 函数持久层
 * @author Jxin
 * @version 1.0
 * @since 2020/9/3 20:35
 */
@Repository
public class FuncRepository implements IFuncRepository {
    private final FuncMapper funcMapper;

    @Autowired
    public FuncRepository(FuncMapper funcMapper) {
        this.funcMapper = funcMapper;
    }

    @Override
    public void saveWhenNone(Func func) {
        final Optional<Func> funcOptional = getFunc(func.getName());
        if(funcOptional.isPresent()){
            return;
        }
        funcMapper.insert(func);
    }

    @Override
    public Optional<Func> getFunc(String name) {
        return Optional.ofNullable(funcMapper.findByName(name));
    }
}
