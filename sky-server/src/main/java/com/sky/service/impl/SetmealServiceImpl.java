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
import com.sky.vo.DishItemVO;
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
    @Autowired
    private SetmealService setmealService;

    /**
     * 套餐修改
     * @param setmealDTO
     */
    @Override
    public void update(SetmealDTO setmealDTO) {
        //修改套餐相关
        Setmeal setmeal=setmealMapper.getById(setmealDTO.getId());
        BeanUtils.copyProperties(setmealDTO,setmeal);

        setmealMapper.update(setmeal);

        //修改套餐与菜品关联
        List<SetmealDish> dishList=setmealDTO.getSetmealDishes();
        setmealDishMapper.deleteDishesBySetmealId(setmealDTO.getId());

        dishList.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealDTO.getId());
            setmealDishMapper.insert(setmealDish);
        });

    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
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

    /**
     * 套餐启用/禁用
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Setmeal setmeal=setmealMapper.getById(id);
        setmeal.setStatus(status);

        setmealMapper.update(setmeal);
    }

    /**
     * 批量删除套餐
     * @param ids
     */
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

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        setmealMapper.insert(setmeal);

        List<SetmealDish> setmealDishes=setmealDTO.getSetmealDishes();

        setmealDishMapper.deleteDishesBySetmealId(setmealDTO.getId());

        setmealDishes.forEach(setmealDish -> {
            setmealDishMapper.insert(setmealDish);
        });
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Override
    public SetmealVO getById(Long id) {
        Setmeal setmeal=setmealMapper.getById(id);
        SetmealVO setmealVO=new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setCategoryName(categoryMapper.getById(setmeal.getCategoryId()).getName());
        setmealVO.setSetmealDishes(setmealDishMapper.getSetmealDishesBySetmealId(setmeal.getId()));
        return setmealVO;
    }


    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

}
