package com.jxin.faas.scheduler.service.exec;

import cn.hutool.core.util.StrUtil;

/**
 * 节点异常类
 * @author Jxin
 * @version 1.0
 * @since 2020/9/1 18:00
 */
public class NodeException extends RuntimeException {
    private static final long serialVersionUID = 6297887085270142405L;
    public NodeException(Throwable e) {
        super(StrUtil.format("{}: {}", e.getClass().getSimpleName(), e.getMessage()), e);
    }

    public NodeException(String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params));
    }

    public NodeException(Throwable throwable, String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params), throwable);
    }
}
