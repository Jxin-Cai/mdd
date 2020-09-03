package com.jxin.faas.scheduler.interfaces.dto.req;

import com.jxin.faas.scheduler.domain.entity.val.FunctionInfoVal;

import java.io.Serializable;

/**
 * 申请容器 请求 dto
 * @author Jxin
 * @version 1.0
 * @since 2020/7/20 17:40
 */
public class AcquireContainerReqDTO implements Serializable {
    /**请求id*/
    private String requestId;
    /**用户id*/
    private String accountId;
    /**函数名*/
    private String functionName;
    /**函数信息*/
    private FunctionInfoVal functionInfo;
}
