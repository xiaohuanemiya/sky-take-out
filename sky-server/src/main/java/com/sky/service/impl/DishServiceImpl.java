package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

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

        dishDTO.getFlavors().forEach(dishFlavor -> {
            dishFlavor.setDishId(dishDTO.getId());
            dishFlavorMapper.insert(dishFlavor);
        });
    }

    @Override
    public void save(DishDTO dishDTO) {
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dish.setUpdateUser(BaseContext.getCurrentId());
        dish.setUpdateTime(LocalDateTime.now());
        dish.setCreateUser(BaseContext.getCurrentId());
        dish.setCreateTime(LocalDateTime.now());

        dishMapper.insert(dish);

        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        dishDTO.getFlavors().forEach(dishFlavor -> {
            dishFlavor.setDishId(dishDTO.getId());
            dishFlavorMapper.insert(dishFlavor);
        });

    }

    /**
     * 菜品批量删除
     * @param ids
     */

    @Override
    public void deleteBatch(List<Long> ids) {
        //判断当前菜品是否能够删除--是否存在起售中的菜品
        for (Long id : ids) {
            Dish dish=dishMapper.getById(id);
            if (Objects.equals(dish.getStatus(), StatusConstant.ENABLE)){
                //当前菜品有处于起售中的
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        
        //判断当前菜品是否能够删除--是否存在菜品被套餐关联
        List<Long> setmealIds=setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds!=null && !setmealIds.isEmpty()){
            //当前菜品被套餐关联了
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //删除菜品表中的菜品数据

        ids.forEach(id -> {
            dishMapper.deleteById(id);
        });

        //删除菜品关联的口味数据
        ids.forEach(id -> {
            dishFlavorMapper.deleteByDishId(id);
        });
    }
}
