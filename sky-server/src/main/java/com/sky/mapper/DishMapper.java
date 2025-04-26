package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    Page<Dish> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    @Select("select * from dish where category_id=#{categoryId}")
    List<Dish> getByCategoryId(Integer categoryId);

    @Select("select * from dish where id=#{id}")
    Dish getById(Long id);

    void update(Dish dish);
}
