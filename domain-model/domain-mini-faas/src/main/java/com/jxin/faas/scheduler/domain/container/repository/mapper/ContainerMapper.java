package com.jxin.faas.scheduler.domain.container.repository.mapper;

import com.jxin.faas.scheduler.domain.container.repository.table.ContainerDO;
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

    int insert(ContainerDO record);

    int insertSelective(ContainerDO record);

    ContainerDO selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ContainerDO record);

    int updateByPrimaryKey(ContainerDO record);

    int updateByContainerId(@Param("updated") ContainerDO updated, @Param("containerId") String containerId);

    int deleteByContainerIdIn(@Param("containerIdCollection") Collection<String> containerIdCollection);

    ContainerDO findByEnabledAndFuncNameOrderByOrder(@Param("enabled") Boolean enabled, @Param("funcName") String funcName);

    Integer countByNodeId(@Param("nodeId") String nodeId);

    Integer countByFuncName(@Param("funcName") String funcName);
    List<String> findContainerIdByNodeIdAndEnabledAndModifyTimeGreaterThan(@Param("nodeId") String nodeId, @Param("enabled") Boolean enabled, @Param("minModifyTime") Date minModifyTime);

    List<String> findContainerIdByNodeIdAndEnabledAndOuttimeLessThan(@Param("nodeId") String nodeId, @Param("enabled") Boolean enabled, @Param("maxOuttime") Date maxOuttime);



}