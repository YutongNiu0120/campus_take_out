package com.campus.take_out.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.take_out.entity.Category;

public interface CategoryService extends IService<Category> {

    public void remove(Long id);
}
