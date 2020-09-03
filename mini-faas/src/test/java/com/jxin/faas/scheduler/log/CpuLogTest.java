package com.jxin.faas.scheduler.log;

import cn.hutool.core.io.FileUtil;
import com.jxin.faas.scheduler.domain.util.IJsonUtil;
import com.jxin.faas.scheduler.infrastructure.util.GsonJsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class CpuLogTest {
    @Test
    public void getCpuJson() {
        final List<String> jsonList = readJsonList("C:\\Users\\LENOVO\\Downloads\\cpu-after.txt");

        /**
         * key：时间
         * value:cpuInfo
         */
        Map<String, String> cpuInfoList = new TreeMap<>();
        for (String s : jsonList) {
            if (s == null || s.trim().equals("")) {
                continue;
            }
            cpuInfoList.put(s.substring(s.indexOf("[") + 1, s.indexOf("[ ")), s.substring(s.indexOf("{"), s.indexOf("}") + 1));
        }
//        System.out.println(cpuInfoList);
        final IJsonUtil jsonUtil = new GsonJsonUtil();
        Map<String, Double> fisrt = null;
        Map<String, Double> last = null;
        boolean flag = true;
        List<String> cpuUsages = new ArrayList<String>();
        for (Map.Entry<String, String> entry : cpuInfoList.entrySet()) {
            if (flag) {
                fisrt = jsonUtil.json2Bean(entry.getValue(), Map.class);
                flag = false;
                continue;
            }
            last = jsonUtil.json2Bean(entry.getValue(), Map.class);
//            System.out.println(entry.getKey() + "=" + cpuUsage(fisrt, last));
            cpuUsages.add(entry.getKey() + "=" + cpuUsage(fisrt, last));
            fisrt = last;
        }

        writeFile("C:\\Users\\LENOVO\\Downloads\\cpu-ratio.txt", cpuUsages);
    }

    public static double cpuUsage(Map<String, Double> map1, Map<String, Double> map2) {
        try {
            double user1 = map1.get("user");
            double nice1 = map1.get("nice");
            double system1 = map1.get("system");
            double idle1 = map1.get("idle");

            double user2 = map2.get("user");
            double nice2 = map2.get("nice");
            double system2 = map2.get("system");
            double idle2 = map2.get("idle");

            double total1 = user1 + system1 + nice1;
            double total2 = user2 + system2 + nice2;
            double total = total2 - total1;

            double totalIdle1 = user1 + nice1 + system1 + idle1;
            double totalIdle2 = user2 + nice2 + system2 + idle2;
            double totalIdle = totalIdle2 - totalIdle1;

            double cpuUsage = (total / totalIdle) * 100;
            return cpuUsage;
        } catch (Exception e) {
            log.error("get cpu usage error :", e);
        }
        return 0;
    }

    private String readJson(String filePath) {
        return FileUtil.readString(new File(filePath), "UTF-8");
    }

    private List<String> readJsonList(String filePath) {
        return FileUtil.readLines(new File(filePath), "UTF-8");
    }

    private void writeFile(String filePath, List<String> rows) {
        //rows.forEach(log::info);
        FileUtil.writeLines(rows, filePath, "utf-8");
    }
}
