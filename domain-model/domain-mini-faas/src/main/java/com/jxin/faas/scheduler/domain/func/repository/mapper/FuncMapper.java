package com.jxin.faas.scheduler.domain.func.repository.mapper;

import com.jxin.faas.scheduler.domain.func.repository.table.Func;
import org.apache.ibatis.annotations.Param;

/**
 * @author Jxin
 * @version 1.0
 * @since 2020/9/3 20:29
 */
public interface FuncMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Func record);

    int insertSelective(Func record);

    Func selectByPrimaryKey(Integer id);

    Func findByName(@Param("name") String name);


    int updateByPrimaryKeySelective(Func record);

    int updateByPrimaryKey(Func record);
}