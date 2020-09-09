package com.jxin.faas.scheduler.infrastructure.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 文件工具测试类
 * @author Jxin
 * @version 1.0
 * @since 2020/9/9 11:57
 */
class FileUtilTest {

    @Test
    void dumpTree() {
        final String tree = FileUtil.dumpTree("E:\\project\\IDEA\\IDEA-B\\demo\\mdd\\mini-faas\\src\\main\\java\\com\\jxin\\faas\\scheduler", 3);
        System.out.println(tree);
    }
}