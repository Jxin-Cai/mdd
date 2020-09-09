package com.jxin.faas.scheduler.repository.mapper;
import java.util.List;

import com.jxin.faas.scheduler.repository.table.Node;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * @author Jxin
 * @version 1.0
 * @since 2020/9/3 20:30
 */
public interface NodeMapper {
    int deleteByPrimaryKey(Integer id);

    int deleteByNodeId(@Param("nodeId")String nodeId);

    int insert(Node record);

    int insertSelective(Node record);

    Node selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Node record);

    int updateByNodeId(@Param("updated")Node updated,@Param("nodeId")String nodeId);

    int updateIdleMemSizeByNodeIdAndIdleMemSize(@Param("nodeId")String nodeId,@Param("idleMemSize")Long idleMemSize);



    int updateByPrimaryKey(Node record);

    Node findByIdleMemSizeGreaterThanAndCpuUsageRatioLessThanOrderByOrder(@Param("minIdleMemSize")Long minIdleMemSize,@Param("maxCpuUsageRatio")BigDecimal maxCpuUsageRatio);

    Integer countByCpuUsageRatioAndMemUsageRatioLessThan(@Param("maxMemUsageRatio")BigDecimal maxMemUsageRatio, @Param("maxCpuUsageRatio")BigDecimal maxCpuUsageRatio);

    List<String> findNodeId();










}