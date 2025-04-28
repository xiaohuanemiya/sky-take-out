package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {
    @Select("select * from setmeal where id=#{id}")
    Setmeal getById(Long id);

    void update(Setmeal setmeal);

    Page<Setmeal> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    @Delete("delete from setmeal where id=#{id}")
    void deleteById(Long id);

    @Select("select * from setmeal where category_id=#{categoryId}")
    List<Setmeal> getByCategory(Long categoryId);
}
