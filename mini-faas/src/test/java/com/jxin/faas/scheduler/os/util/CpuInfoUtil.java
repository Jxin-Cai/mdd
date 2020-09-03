package com.jxin.faas.scheduler.os.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

@Slf4j
public class CpuInfoUtil {
    public static void main(String[] args) {
        cpuInfo();
    }

    public static int cpuUsage() {
        try {
            Map<String, Object> map1 = cpuInfo();
            Thread.sleep(5 * 1000);
            Map<String, Object> map2 = cpuInfo();

            long user1 = Long.parseLong(map1.get("user").toString());
            long nice1 = Long.parseLong(map1.get("nice").toString());
            long system1 = Long.parseLong(map1.get("system").toString());
            long idle1 = Long.parseLong(map1.get("idle").toString());

            long user2 = Long.parseLong(map2.get("user").toString());
            long nice2 = Long.parseLong(map2.get("nice").toString());
            long system2 = Long.parseLong(map2.get("system").toString());
            long idle2 = Long.parseLong(map2.get("idle").toString());

            long total1 = user1 + system1 + nice1;
            long total2 = user2 + system2 + nice2;
            float total = total2 - total1;

            long totalIdle1 = user1 + nice1 + system1 + idle1;
            long totalIdle2 = user2 + nice2 + system2 + idle2;
            float totalIdle = totalIdle2 - totalIdle1;

            float cpuUsage = (total / totalIdle) * 100;
            return (int) cpuUsage;
        } catch (Exception e) {
            log.error("get cpu usage error :", e);
        }
        return 0;
    }

    public static Map<String, Object> cpuInfo() {
        Map<String, Object> map = new HashMap<String, Object>();
        try (InputStreamReader inputs = new InputStreamReader(new FileInputStream("/proc/stat"));
             BufferedReader buffer = new BufferedReader(inputs);) {
            String line = "";
            while (true) {
                line = buffer.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith("cpu")) {
                    StringTokenizer tokenizer = new StringTokenizer(line);
                    List<String> temp = new ArrayList<String>();
                    while (tokenizer.hasMoreElements()) {
                        String value = tokenizer.nextToken();
                        temp.add(value);
                    }
                    map.put("user", temp.get(1));
                    map.put("nice", temp.get(2));
                    map.put("system", temp.get(3));
                    map.put("idle", temp.get(4));
                    map.put("iowait", temp.get(5));
                    map.put("irq", temp.get(6));
                    map.put("softirq", temp.get(7));
                    map.put("stealstolen", temp.get(8));
                    break;
                }
            }
        } catch (Exception e) {
            log.error("get cpuInfo error:", e);
        }
        return map;
    }
}
