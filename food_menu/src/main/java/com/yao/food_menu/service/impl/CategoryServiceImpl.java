package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.entity.Category;
import com.yao.food_menu.mapper.CategoryMapper;
import com.yao.food_menu.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private com.yao.food_menu.service.DishCategoryService dishCategoryService;

    @Override
    public List<Category> listWithDishCount(Category category) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        queryWrapper.eq(category.getFamilyId() != null, Category::getFamilyId, category.getFamilyId());
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = this.list(queryWrapper);

        if (list != null && !list.isEmpty()) {
            for (Category c : list) {
                // 使用dish_category中间表统计菜品数量
                List<Long> dishIds = dishCategoryService.getDishIdsByCategoryId(c.getId());
                c.setDishCount((long) dishIds.size());
            }
        }

        return list;
    }
}
