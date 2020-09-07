package com.jxin.faas.scheduler.log;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.jxin.faas.scheduler.infrastructure.util.IJsonUtil;
import com.jxin.faas.scheduler.infrastructure.util.impl.GsonJsonUtil;
import io.grpc.netty.shaded.io.netty.util.CharsetUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 一些简单玩意的测试类
 * @author Jxin
 * @version 1.0
 * @since 2020/7/21 19:25
 */
@Slf4j
public class LogTest {

    @Test
    public void dumpLog(){
        try {
            final IJsonUtil jsonUtil = new GsonJsonUtil();
            final List<String> jsonList = readJsonList("E:\\download\\google\\231793_351856_1117827");
            final List<List<String>> split = CollUtil.split(jsonList, 50000);
            for (int i = 0; i < split.size(); i++) {
                final List<Log> logs =
                        split.get(i).stream().map(s -> jsonUtil.json2Bean(s, Log.class)).collect(Collectors.toList());
                logs.forEach(this::lineHandler);

                logs.sort(Comparator.comparing(Log::getTime));

                writeFile("E:\\download\\google\\mini-faas-" + i + ".log",
                        logs.stream().map(Log::getLog).collect(Collectors.toList()));
            }
        }catch (Exception e){

        }
    }


    @Test
    @SneakyThrows
    public void dumpLineLog(){
        final IJsonUtil jsonUtil = new GsonJsonUtil();
        FileUtil.readLines(new RandomAccessFile(new File("E:\\download\\google\\231793_351856_1106713"), "rws"), CharsetUtil.UTF_8,line -> {
            final Log log = jsonUtil.json2Bean(line, Log.class);
            lineHandler(log);
            writeLineFile("E:\\download\\google\\mini-faas-lines.log", log.getLog());
        });
    }
    private void lineHandler(Log log) {
        final String logStr = log.getLog();
        final String replace = logStr.replace("\u001b", "")
                //.replace("\r\n", "")
                .replace("2m", "")
                .replace("0;39m", "")
                .replace("35m", "")
                .replace("36m", "")
                .replace("32m", "");
        log.setLog(replace);
    }

    private List<String> readJsonList(String filePath){
        return FileUtil.readLines(new File(filePath), "UTF-8");
    }
    private void writeFile(String filePath, List<String> rows){
        //rows.forEach(log::info);
        FileUtil.appendLines(rows, filePath, "utf-8");
    }
    private void writeLineFile(String filePath, String line){
        //rows.forEach(log::info);
        FileUtil.appendString(line, filePath, "utf-8");
    }
}
