package com.campus.take_out.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.take_out.dto.SetmealDto;
import com.campus.take_out.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    public void saveWithDish(SetmealDto setmealDto);

    public void removeWithDish(List<Long> ids);

    public SetmealDto getByIdWithDish(Long id);

    public void updateWithDish(SetmealDto setmealDto);

    public void updateStatus(int status, List<Long> ids);
}
