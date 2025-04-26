package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    List<Dish> list(Integer categoryId);

    DishVO getById(Long id);

    void update(DishDTO dishDTO);

    void save(DishDTO dishDTO);

    void deleteBatch(List<Long> ids);

    void startOrStop(Integer status, Long id);
}
