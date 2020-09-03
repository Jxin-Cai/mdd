package com.jxin.faas.scheduler.domain.entity.dmo;

import com.jxin.faas.scheduler.domain.entity.val.FunctionInfoVal;
import lombok.SneakyThrows;
import org.junit.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.Assert.*;

/**
 * @author Jxin
 * @version 1.0
 * @since 2020/7/30 19:23
 */
public class FunctionTest {

    @Test
    @SneakyThrows
    public void refreshLastTime() {

        final Executor executor = Executors.newFixedThreadPool(10);
        final Function function = Function.of(100, 1000, new FunctionInfoVal());
        executor.execute(() -> function.refreshLastTime());
        executor.execute(() -> function.refreshLastTime());
        executor.execute(() -> function.refreshLastTime());
        executor.execute(() -> function.refreshLastTime());
        executor.execute(() -> function.refreshLastTime());
        executor.execute(() -> function.refreshLastTime());
        executor.execute(() -> function.refreshLastTime());
        executor.execute(() -> function.refreshLastTime());





        Thread.sleep(100000);
    }
}