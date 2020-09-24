package com.jxin.faas.scheduler.domain.container.repository.persistence.impl;

import com.jxin.faas.scheduler.domain.container.repository.mapper.FuncMapper;
import com.jxin.faas.scheduler.domain.container.repository.persistence.IFuncRepository;
import com.jxin.faas.scheduler.domain.container.repository.table.FuncDO;
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
    public void saveWhenNone(FuncDO func) {
        final Optional<FuncDO> funcOptional = getFunc(func.getName());
        if(funcOptional.isPresent()){
            return;
        }
        funcMapper.insert(func);
    }

    @Override
    public Optional<FuncDO> getFunc(String name) {
        return Optional.ofNullable(funcMapper.findByName(name));
    }
}
