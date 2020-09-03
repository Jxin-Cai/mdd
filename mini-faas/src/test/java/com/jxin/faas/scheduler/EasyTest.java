package com.jxin.faas.scheduler;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.jxin.faas.scheduler.domain.entity.dmo.Node;
import lombok.SneakyThrows;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * 一些简单玩意的测试类
 * @author Jxin
 * @version 1.0
 * @since 2020/7/21 19:25
 */
public class EasyTest {
    private static final Map<String, String> ID_LOCK = Maps.newConcurrentMap();
    private static final Map<String, Semaphore> NEW_ID_LOCK = new ConcurrentHashMap<>(20);

    @Test
    public void testConcurrentHashMap(){
        final Set<Integer> set = new ConcurrentHashSet<>();
        for (int i = 0; i < 1000; i++) {
            set.add(i);
        }
        final Executor executor = Executors.newFixedThreadPool(30);
        executor.execute(() -> {
            for (int i = 1000; i < 5000; i++) {
                set.add(i);
            }
        });
        for (int i = 2; i < 5; i++) {
            final int val = i;
            executor.execute(() -> {
                for (Integer integer : set) {
                    if(integer % val == 0){
                        set.remove(integer);
                    }
                }
            });
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(set);
    }
    @Test
    public void testIdLock() throws InterruptedException {
        final Executor executor = Executors.newFixedThreadPool(30);
        for (int i = 0; i < 20; i++) {
            Thread.sleep(3000);
            executor.execute(() -> {
                final String threadName = Thread.currentThread().getName();
                final String lockVal = ID_LOCK.putIfAbsent("a", threadName);
                if(!threadName.equals(lockVal)){
                    System.out.println("a获取锁失败");
                    return;
                }

                try {
                    Thread.sleep(1);
                    ID_LOCK.remove("a");
                    System.err.println("a获取锁成功");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    ID_LOCK.remove("a");
                }
            });
            executor.execute(() -> {
                final String threadName = Thread.currentThread().getName();
                final String lockVal = ID_LOCK.putIfAbsent("b", threadName);
                if(!threadName.equals(lockVal)){
                    System.out.println("b获取锁失败");
                    return;
                }
                try {
                    Thread.sleep(1);
                    System.err.println("b获取锁成功");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    ID_LOCK.remove("b");
                }

            });
        }
    }
    @Test
    public void testNewIdLock() throws InterruptedException {
        final Executor executor = Executors.newFixedThreadPool(30);
        for (int i = 0; i < 20; i++) {
            Thread.sleep(3000);
            executor.execute(() -> {

                final Semaphore a = NEW_ID_LOCK.computeIfAbsent("a", s -> new Semaphore(1));
                if(!a.tryAcquire()){
                    System.out.println("a获取锁失败");
                    return;
                }

                try {
                    Thread.sleep(1);
                    ID_LOCK.remove("a");
                    System.err.println("a获取锁成功");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                   a.release();
                }
            });
            executor.execute(() -> {
                final Semaphore b = NEW_ID_LOCK.computeIfAbsent("b", s -> new Semaphore(1));
                if(!b.tryAcquire()){
                    System.out.println("b获取锁失败");
                    return;
                }
                try {
                    Thread.sleep(1);
                    System.err.println("b获取锁成功");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                  b.release();
                }

            });
        }
    }
    @Test
    public void testBigDecimal(){
        final BigDecimal decimal1 = BigDecimal.valueOf(1);
        final BigDecimal decimal2 = BigDecimal.valueOf(2);
        System.out.println(decimal1.compareTo(decimal2));
        System.out.println(decimal2.compareTo(decimal1));
    }
    @Test
    public void test(){
        final Map<String, Node> nodeStatMap = Maps.newHashMap();
        nodeStatMap.put("a1a1", new Node());
        System.out.println( MapUtil.join(nodeStatMap, StrUtil.COMMA, StrUtil.COLON, true));

    }
    @Test
    public void testSys(){
        final String config_subfix = System.getenv("CONFIG_SUBFIX");
        final String resource_manager_endpoint = System.getenv("RESOURCE_MANAGER_ENDPOINT");
        System.out.println(config_subfix);
        System.out.println(resource_manager_endpoint);
    }
    @Test
    public void testNode(){
        Set<Integer> nodes = new ConcurrentSkipListSet<>();
        final boolean remove = nodes.remove(5);
        System.out.println(remove);

    }
    @Test
    public void testFinally(){
        for (int i = 0; i < 10; i++) {
            try {
                if(i == 5){
                    continue;
                }
            }finally {
                System.out.println("拿到: " + i);
            }
        }

    }
    @Test
    public void testCast(){
        final String bool = "true";
        final String i = "1";

        System.out.println(Boolean.valueOf(bool));
        System.out.println(Integer.class.cast(Integer.valueOf(i)));
    }
}
