package com.jxin.faas.scheduler.repository.persistence;

import com.jxin.faas.scheduler.repository.table.Func;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

/**
 * 函数持久层接口 测试类
 * @author Jxin
 * @version 1.0
 * @since 2020/9/7 19:38
 */
@RunWith(SpringRunner.class)
class IFuncRepositoryTest {
    @Autowired
    private IFuncRepository funcRepository;

    @Test
    void saveWhenNone() {
        final Func func = Func.of("test", 1000L, "test", 1);
        funcRepository.saveWhenNone(func);
        final Optional<Func> func1 = funcRepository.getFunc(func.getName());
        Assert.assertTrue(func1.isPresent());
        funcRepository.saveWhenNone(func);
        final Optional<Func> func2 = funcRepository.getFunc(func.getName());
        Assert.assertTrue(func2.isPresent());
        Assert.assertEquals(func1.get().getId(), func2.get().getId());
    }

    @Test
    void getFunc() {
        final Func func = Func.of("test", 1000L, "test", 1);
        funcRepository.saveWhenNone(func);
        final Optional<Func> func1 = funcRepository.getFunc(func.getName());
        Assert.assertTrue(func1.isPresent());
        Assert.assertEquals(func1.get().getName(), func.getName());
    }
}