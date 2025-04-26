package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private CategoryMapper categoryMapper;


    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());

        Page<Dish> page=dishMapper.pageQuery(dishPageQueryDTO);
        Page<DishVO> dishVOPage = new Page<>();
        for (int i=0;i<page.getResult().size();i++){
            DishVO dishVO=new DishVO();
            Dish dish=page.getResult().get(i);
            BeanUtils.copyProperties(dish,dishVO);
            dishVO.setCategoryName(categoryMapper.getById(dish.getCategoryId()).getName());
            dishVOPage.add(dishVO);
        }

        long total = page.getTotal();
        List<DishVO> records = dishVOPage.getResult();

        return new PageResult(total,records);
    }

    @Override
    public List<Dish> list(Integer categoryId) {
        return dishMapper.getByCategoryId(categoryId);
    }


    private String getCategoryName(Long categoryId) {
        return categoryMapper.getById(categoryId).getName();
    }

    private List<DishFlavor> getFlavorsByDishId(Long dishId) {
        return dishFlavorMapper.getByDishId(dishId);
    }

    @Override
    public DishVO getById(Long id) {
        Dish dish=dishMapper.getById(id);
        DishVO dishVO=new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setCategoryName(getCategoryName(dish.getCategoryId()));
        dishVO.setFlavors(getFlavorsByDishId(dish.getId()));
        return dishVO;
    }


    private void deleteFlavorsByDishId(Long dishId){
        dishFlavorMapper.deleteByDishId(dishId);
    }


    @Override
    public void update(DishDTO dishDTO) {
        Dish dish=dishMapper.getById(dishDTO.getId());
        BeanUtils.copyProperties(dishDTO,dish);
        dish.setUpdateTime(LocalDateTime.now());
        dish.setUpdateUser(BaseContext.getCurrentId());
        dishMapper.update(dish);
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        List<DishFlavor> dishFlavors=dishDTO.getFlavors();
        for(DishFlavor flavor:dishFlavors){
            flavor.setDishId(dishDTO.getId());
            log.info("{}",flavor);
            dishFlavorMapper.insert(flavor);
        }
    }
}
