package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.entity.DishFlavor;
import com.yao.food_menu.mapper.DishFlavorMapper;
import com.yao.food_menu.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
