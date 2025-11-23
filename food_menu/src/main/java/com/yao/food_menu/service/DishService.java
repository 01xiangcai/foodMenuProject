package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.dto.DishDto;
import com.yao.food_menu.entity.Dish;

/**
 * 菜品服务接口
 */
public interface DishService extends IService<Dish> {

    // 保存菜品及口味
    public void saveWithFlavor(DishDto dishDto);

    // 根据ID获取菜品及口味
    public DishDto getByIdWithFlavor(Long id);

    // 更新菜品及口味
    public void updateWithFlavor(DishDto dishDto);
}
