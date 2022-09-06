package com.campus.take_out.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.take_out.dto.DishDto;
import com.campus.take_out.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    public void saveWithFlavor(DishDto dishDto);

    public DishDto getByIdWithFlavor(Long id);

    public void updateWithFlavor(DishDto dishDto);

    public void removeByIdsWithFlavor(List<Long> ids);


    public void updateStatus(int status, List<Long> ids);
}
