package com.jxin.faas.scheduler.log;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.jxin.faas.scheduler.infrastructure.util.IJsonUtil;
import com.jxin.faas.scheduler.infrastructure.util.impl.GsonJsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import schedulerproto.ReturnContainerRequest;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 归还日志分析类
 * @author Jxin
 * @version 1.0
 * @since 2020/7/30 16:59
 */
@Slf4j
public class ReturnLogTest {
    @Test
    public void analyzeReturnLog(){
        final IJsonUtil jsonUtil = new GsonJsonUtil();
        final List<String> jsonList = readJsonList("E:\\download\\google\\return.log");

        final List<ReturnContainerRequest> returnList =
                jsonList.stream()
                        .map(json -> StrUtil.subSuf(json, json.indexOf("{")))
                        .map(json -> jsonUtil.json2Bean(json, ReturnContainerRequest.class)).collect(Collectors.toList());

        final Map<String, List<ReturnContainerRequest>> collect =
                returnList.stream().collect(Collectors.toMap(ReturnContainerRequest::getErrorCode, Lists::newArrayList, (o, o2) -> {
                    final List<ReturnContainerRequest> result = Lists.newArrayListWithCapacity(o.size() + o2.size());
                    result.addAll(o);
                    result.addAll(o2);
                    return result;
                }));

        collect.forEach((k, v)->{
            final ReturnContainerRequest first = CollUtil.getFirst(v);
            log.info("异常类型: {}, 异常信息: {}, 异常数量: {}", first.getErrorCode(), first.getErrorMessage(), v.size());
        });
    }

    private List<String> readJsonList(String filePath){
        return FileUtil.readLines(new File(filePath), "UTF-8");
    }
}
