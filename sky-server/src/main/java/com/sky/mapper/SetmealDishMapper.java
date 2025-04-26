package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

/*
    @Select("select setmeal_id from setmeal_dish where dish_id in (${dishIds})")
*/
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);


}
