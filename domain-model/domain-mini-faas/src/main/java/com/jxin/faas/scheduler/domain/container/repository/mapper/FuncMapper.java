package com.jxin.faas.scheduler.domain.container.repository.mapper;

import com.jxin.faas.scheduler.domain.container.repository.table.FuncDO;
import org.apache.ibatis.annotations.Param;

/**
 * @author Jxin
 * @version 1.0
 * @since 2020/9/3 20:29
 */
public interface FuncMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(FuncDO record);

    int insertSelective(FuncDO record);

    FuncDO selectByPrimaryKey(Integer id);

    FuncDO findByName(@Param("name") String name);


    int updateByPrimaryKeySelective(FuncDO record);

    int updateByPrimaryKey(FuncDO record);
}