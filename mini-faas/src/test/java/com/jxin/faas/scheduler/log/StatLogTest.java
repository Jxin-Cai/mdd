package com.jxin.faas.scheduler.log;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.google.gson.internal.LinkedTreeMap;
import com.jxin.faas.scheduler.domain.entity.dmo.Node;
import com.jxin.faas.scheduler.domain.util.IJsonUtil;
import com.jxin.faas.scheduler.infrastructure.util.GsonJsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 归还日志测试类
 * @author Jxin
 * @version 1.0
 * @since 2020/7/30 16:59
 */
@Slf4j
public class StatLogTest {
    @Test
    public void analyzeStatLog(){
        final IJsonUtil jsonUtil = new GsonJsonUtil();
        final List<String> jsonList = readJsonList("E:\\download\\google\\当前节点状态");
        final List<List<LinkedTreeMap>> collect = jsonList.stream()
                .map(json -> jsonUtil.json2BeanList(StrUtil.subSuf(json, json.lastIndexOf("nodes:") + 7), LinkedTreeMap.class)).collect(Collectors.toList());

        final List<List<LinkedTreeMap>> collect1 = collect.stream()
                .filter(nodes -> nodes.stream().anyMatch(map ->
                        new BigDecimal((Double) map.get("cpuUsageRatio")).compareTo(BigDecimal.valueOf(100)) > 0))
                .collect(Collectors.toList());

        for (List<LinkedTreeMap> linkedTreeMaps : collect1) {
            System.out.println(jsonUtil.beanJson(linkedTreeMaps));
        }

    }

    private List<String> readJsonList(String filePath){
        return FileUtil.readLines(new File(filePath), "UTF-8");
    }
}
