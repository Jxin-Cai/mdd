package com.jxin.faas.scheduler.interfaces.dto.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 归还容器 请求 dto
 * @author Jxin
 * @version 1.0
 * @since 2020/7/20 17:40
 */
@Data
public class ReturnContainerReqDTO implements Serializable {
   /**请求Id*/
   private String requestId;
   /**容器Id*/
   private String containerId;
   /**持续时间 ns*/
   private Integer durationTime;
   /**最大内存使用大小*/
   private Integer maxMemoryUsageSize;
   /**异常code*/
   private String errCode;
    /**异常信息*/
   private String errMsg;
}
