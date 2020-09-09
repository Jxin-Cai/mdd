package com.jxin.faas.scheduler.infrastructure.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * 文件工具
 * @author Jxin
 * @version 1.0
 * @since 2020/9/9 10:28
 */
public class FileUtil {

    public static String dumpTree(String filePath, int maxLevel) {
        return treeDir(new File(filePath), 0 , maxLevel);
    }


    private static String treeDir(File f, int level, int maxLevel) {
        if (!f.isDirectory() || level == maxLevel) {
            return null;
        }
        String ret = "";
        for (int i = 0; i < level; i++) {
            ret += "|  ";
        }
        ret += "|--" + f.getName();
        final File[] childs = f.listFiles();
        if (null == childs || ArrayUtils.isEmpty(childs)) {
            return ret;
        }

        for (File child : childs) {
            if (child.isDirectory()) {
                final String childStr = treeDir(child, level + 1, maxLevel);
                if(StringUtils.isBlank(childStr)){
                    continue;
                }
                ret += "\n" + childStr;
            }
        }
        return ret;
    }
}