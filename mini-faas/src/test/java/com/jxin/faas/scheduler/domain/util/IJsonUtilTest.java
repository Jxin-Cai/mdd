package com.jxin.faas.scheduler.domain.util;

import com.google.common.collect.Maps;
import com.jxin.faas.scheduler.domain.entity.dmo.Node;
import com.jxin.faas.scheduler.infrastructure.util.GsonJsonUtil;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * json工具
 * @author Jxin
 * @version 1.0
 * @since 2020/7/24 20:14
 */
public class IJsonUtilTest {


    @Test
    public void beanJson() {
        final Map<String, Node> map = Maps.newHashMap();
        map.put("a", new Node());
        final IJsonUtil jsonUtil = new GsonJsonUtil();
        System.out.println(jsonUtil.beanJson(map));

    }
}