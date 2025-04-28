package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.BaseException;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;
    /**
     * 套餐修改
     * @param setmealDTO
     */
    @Override
    public void update(SetmealDTO setmealDTO) {
        //修改套餐相关
        Setmeal setmeal=setmealMapper.getById(setmealDTO.getId());
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmeal.setUpdateTime(LocalDateTime.now());
        setmeal.setUpdateUser(BaseContext.getCurrentId());
        setmealMapper.update(setmeal);

        //修改套餐与菜品关联
        List<SetmealDish> dishList=setmealDTO.getSetmealDishes();
        setmealDishMapper.deleteDishesBySetmealId(setmealDTO.getId());

        dishList.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealDTO.getId());
            setmealDishMapper.insert(setmealDish);
        });

    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<Setmeal> page=setmealMapper.pageQuery(setmealPageQueryDTO);

        Page<SetmealVO> setmealVOPage = new Page<>();
        for (int i=0;i<page.getResult().size();i++){
            SetmealVO setmealVO=new SetmealVO();
            Setmeal setmeal=page.getResult().get(i);
            BeanUtils.copyProperties(setmeal,setmealVO);
            setmealVO.setCategoryName(categoryMapper.getById(setmeal.getCategoryId()).getName());
            setmealVOPage.add(setmealVO);
        }

        long total = page.getTotal();
        List<SetmealVO> records = setmealVOPage.getResult();

        return new PageResult(total,records);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Setmeal setmeal=setmealMapper.getById(id);
        setmeal.setStatus(status);
        setmeal.setUpdateUser(BaseContext.getCurrentId());
        setmeal.setUpdateTime(LocalDateTime.now());
        setmealMapper.update(setmeal);
    }

    @Override
    public void deleteBatch(Long[] ids) {
        //判断当前套餐是否能够删除--是否存在起售中的套餐
        for (Long id : ids) {
            Setmeal setmeal=setmealMapper.getById(id);
            if (setmeal!=null && Objects.equals(setmeal.getStatus(), StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        //删除套餐表中的套餐数据
        for (Long id : ids) {
            setmealMapper.deleteById(id);
        }

    }
}
