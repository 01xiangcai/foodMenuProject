package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.dto.DishDto;
import com.yao.food_menu.entity.Dish;

public interface DishService extends IService<Dish> {

    // Save dish with flavor
    public void saveWithFlavor(DishDto dishDto);

    // Get dish with flavor by id
    public DishDto getByIdWithFlavor(Long id);

    // Update dish with flavor
    public void updateWithFlavor(DishDto dishDto);
}
