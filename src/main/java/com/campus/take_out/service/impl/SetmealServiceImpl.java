package com.campus.take_out.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.take_out.common.CustomException;
import com.campus.take_out.dto.SetmealDto;
import com.campus.take_out.entity.Dish;
import com.campus.take_out.entity.Setmeal;
import com.campus.take_out.entity.SetmealDish;
import com.campus.take_out.mapper.SetmealMapper;
import com.campus.take_out.service.DishService;
import com.campus.take_out.service.SetmealDishService;
import com.campus.take_out.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private DishService dishService;

    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        this.save(setmealDto);
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map(item -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     *
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
//        查询套餐状态
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId, ids).eq(Setmeal::getStatus, 1);
        int count = this.count(setmealLambdaQueryWrapper);
        if (count > 0) {
            throw new CustomException("套餐正在售卖中，不能删除");
        }
//        删除setmeal
        this.removeByIds(ids);
//        删除setmeal_dish
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(setmealDishLambdaQueryWrapper);
    }

    @Transactional
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        Setmeal setmeal = this.getById(id);

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        queryWrapper.orderByDesc(SetmealDish::getUpdateTime);
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);
        setmealDto.setSetmealDishes(list);

        return setmealDto;
    }

    @Transactional
    @Override
    public void updateWithDish(SetmealDto setmealDto) {
//        更新setmeal
        this.updateById(setmealDto);
//        删除setmeal dish
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);
//        插入setmeal dish
        List<SetmealDish> list = setmealDto.getSetmealDishes();
//        为每个dish设置setmealId
//        逻辑删除后原来id还在，因此要设置成null，保存时自动重新生成id
        list.stream().map(item -> {
            item.setSetmealId(setmealDto.getId());
            item.setId(null);
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(list);
    }

    @Override
    public void updateStatus(int status, List<Long> ids) {
//      菜品中有停售的，不能启售套餐
        if(status==1){
            LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
            setmealDishLambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
            List<SetmealDish> setmealDishList = setmealDishService.list(setmealDishLambdaQueryWrapper);
            Set<Long> dishIds = new HashSet<>();
            setmealDishList.stream().map(item -> {
                dishIds.add(item.getDishId());
                return item;
            }).collect(Collectors.toList());
            LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishLambdaQueryWrapper.in(Dish::getId, dishIds).eq(Dish::getStatus, 0);
            int count = dishService.count(dishLambdaQueryWrapper);
            if(count>0){
                throw new CustomException("套餐包含停售菜品，请先启售");
            }
        }
        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Setmeal::getId, ids);
        updateWrapper.set(Setmeal::getStatus, status);
        this.update(updateWrapper);
    }
}

























