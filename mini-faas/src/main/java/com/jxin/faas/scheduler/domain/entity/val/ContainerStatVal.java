package com.jxin.faas.scheduler.domain.entity.val;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 容器状态信息  值对象
 * @author Jxin
 * @version 1.0
 * @since 2020/7/20 21:21
 */
@Data
public class ContainerStatVal implements Serializable {
   /**容器Id*/
   private String containerId;
   /**内存总占用*/
   private Long memoryAllSize;
   /**已使用内存*/
   private Long memoryUsageSize;
   /**已使用cpu百分比*/
   private BigDecimal cpuUsageRatio;

   public ContainerStatVal() {
   }

   private ContainerStatVal(String containerId, Long memoryAllSize, Long memoryUsageSize, BigDecimal cpuUsageRatio) {
      this.containerId = containerId;
      this.memoryAllSize = memoryAllSize;
      this.memoryUsageSize = memoryUsageSize;
      this.cpuUsageRatio = cpuUsageRatio;
   }
   public static ContainerStatVal of(String containerId, Long memoryAllSize, Long memoryUsageSize, BigDecimal cpuUsageRatio){
      return new ContainerStatVal(containerId, memoryAllSize, memoryUsageSize, cpuUsageRatio);
   }
}
