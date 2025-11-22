package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.entity.Category;
import com.yao.food_menu.mapper.CategoryMapper;
import com.yao.food_menu.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
}
