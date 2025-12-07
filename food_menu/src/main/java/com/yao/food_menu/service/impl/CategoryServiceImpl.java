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
    private com.yao.food_menu.service.DishService dishService;

    @Override
    public List<Category> listWithDishCount(Category category) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        queryWrapper.eq(category.getFamilyId() != null, Category::getFamilyId, category.getFamilyId());
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = this.list(queryWrapper);

        if (list != null && !list.isEmpty()) {
            for (Category c : list) {
                LambdaQueryWrapper<com.yao.food_menu.entity.Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
                dishQueryWrapper.eq(com.yao.food_menu.entity.Dish::getCategoryId, c.getId());
                // 这里统计所有状态的菜品，如果只想统计在售的，可以加 eq(Dish::getStatus, 1)
                // 暂时统计所有
                long count = dishService.count(dishQueryWrapper);
                c.setDishCount(count);
            }
        }

        return list;
    }
}
