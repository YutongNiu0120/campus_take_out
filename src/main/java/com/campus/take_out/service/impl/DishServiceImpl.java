package com.campus.take_out.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.take_out.common.CustomException;
import com.campus.take_out.dto.DishDto;
import com.campus.take_out.entity.Dish;
import com.campus.take_out.entity.DishFlavor;
import com.campus.take_out.entity.Setmeal;
import com.campus.take_out.entity.SetmealDish;
import com.campus.take_out.mapper.DishMapper;
import com.campus.take_out.service.DishFlavorService;
import com.campus.take_out.service.DishService;
import com.campus.take_out.service.SetmealDishService;
import com.campus.take_out.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Transactional
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        this.save(dishDto);

        Long dishId = dishDto.getId();

        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map(item -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public DishDto getByIdWithFlavor(Long id) {
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        this.updateById(dishDto);

//      清理当前flavor
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());

        dishFlavorService.remove(queryWrapper);

//        插入新flavor
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map(item -> {
            item.setDishId(dishDto.getId());
            item.setId(null);
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    @Override
    @Transactional
    public void removeByIdsWithFlavor(List<Long> ids) {
//        启动售的菜品不能删除
        LambdaQueryWrapper<Dish> dishStatusQueryWrapper = new LambdaQueryWrapper<>();
        dishStatusQueryWrapper.in(Dish::getId, ids).eq(Dish::getStatus, 1);
        int saleCount = this.count(dishStatusQueryWrapper);
        if (saleCount > 0) {
            throw new CustomException("不能删除正在售卖的菜品，请先停售");
        }
//      包含在套餐中的菜品不能删除
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getDishId, ids);
        int setmealCount = setmealDishService.count(setmealDishLambdaQueryWrapper);
        if(setmealCount>0){
            throw new CustomException("不能删除套餐中的菜品，请先删除套餐");
        }

//        删除dish
        this.removeByIds(ids);
//        删除flavor
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DishFlavor::getDishId, ids);
        dishFlavorService.remove(queryWrapper);
    }

    @Override
    public void updateStatus(int status, List<Long> ids) {
//      有在售卖的套餐包含菜品 不能停售菜品
        if (status == 0) {
            LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
            setmealDishLambdaQueryWrapper.in(SetmealDish::getDishId, ids);
            List<SetmealDish> setmealDishList = setmealDishService.list(setmealDishLambdaQueryWrapper);
//            setmealDishList为空说明没有套餐
            if(!setmealDishList.isEmpty()){
                Set<Long> setmealIds = new HashSet<>();
                setmealDishList.stream().map(item -> {
                    setmealIds.add(item.getSetmealId());
                    return item;
                }).collect(Collectors.toList());

                LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
                setmealLambdaQueryWrapper.in(Setmeal::getId, setmealIds).eq(Setmeal::getStatus, 1);
                int count = setmealService.count(setmealLambdaQueryWrapper);
                if (count > 0) {
                    throw new CustomException("有正在售卖的套餐中包含的菜品，请先停售套餐");
                }
            }
        }
        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Dish::getId, ids);
        updateWrapper.set(Dish::getStatus, status);
        this.update(updateWrapper);
    }
}
