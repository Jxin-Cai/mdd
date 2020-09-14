package com.jxin.faas.scheduler.domain.container.repository.mapper;

import com.jxin.faas.scheduler.domain.container.repository.table.Container;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Jxin
 * @version 1.0
 * @since 2020/9/4 19:29
 */
public interface ContainerMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Container record);

    int insertSelective(Container record);

    Container selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Container record);

    int updateByPrimaryKey(Container record);

    int updateByContainerId(@Param("updated") Container updated, @Param("containerId") String containerId);

    int deleteByContainerIdIn(@Param("containerIdCollection") Collection<String> containerIdCollection);

    Container findByEnabledAndFuncNameOrderByOrder(@Param("enabled") Boolean enabled, @Param("funcName") String funcName);

    Integer countByNodeId(@Param("nodeId") String nodeId);

    Integer countByFuncName(@Param("funcName") String funcName);
    List<String> findContainerIdByNodeIdAndEnabledAndModifyTimeGreaterThan(@Param("nodeId") String nodeId, @Param("enabled") Boolean enabled, @Param("minModifyTime") Date minModifyTime);

    List<String> findContainerIdByNodeIdAndEnabledAndOuttimeLessThan(@Param("nodeId") String nodeId, @Param("enabled") Boolean enabled, @Param("maxOuttime") Date maxOuttime);



}