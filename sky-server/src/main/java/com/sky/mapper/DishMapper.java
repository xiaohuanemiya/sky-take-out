package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
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

    @Insert("insert into dish(name, category_id, price, image, description, status, create_time, update_time, create_user, update_user) " +
            "values(#{name},#{categoryId},#{price},#{image},#{description},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void insert(Dish dish);

    /**
     * 根据主键删除菜品
     * @param id
     */

    @Delete("delete from dish where id=#{id}")
    void deleteById(Long id);
}
