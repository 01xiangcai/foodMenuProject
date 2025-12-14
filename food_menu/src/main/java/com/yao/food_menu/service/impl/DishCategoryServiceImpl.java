package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.common.context.FamilyContext;
import com.yao.food_menu.entity.DishCategory;
import com.yao.food_menu.mapper.DishCategoryMapper;
import com.yao.food_menu.service.DishCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品分类关联 Service 实现类
 */
@Service
@Slf4j
public class DishCategoryServiceImpl extends ServiceImpl<DishCategoryMapper, DishCategory>
        implements DishCategoryService {

    @Override
    @Transactional
    public void saveDishCategories(Long dishId, List<Long> categoryIds) {
        if (dishId == null || categoryIds == null || categoryIds.isEmpty()) {
            return;
        }

        Long familyId = FamilyContext.getFamilyId();

        // 构建菜品分类关联列表
        List<DishCategory> dishCategories = categoryIds.stream()
                .distinct() // 去重
                .map(categoryId -> {
                    DishCategory dishCategory = new DishCategory();
                    dishCategory.setDishId(dishId);
                    dishCategory.setCategoryId(categoryId);
                    dishCategory.setFamilyId(familyId);
                    return dishCategory;
                })
                .collect(Collectors.toList());

        // 批量保存
        this.saveBatch(dishCategories);
        log.info("保存菜品分类关联 - 菜品ID: {}, 分类数量: {}", dishId, dishCategories.size());
    }

    @Override
    @Transactional
    public void updateDishCategories(Long dishId, List<Long> categoryIds) {
        if (dishId == null) {
            return;
        }

        // 删除旧的分类关联
        LambdaQueryWrapper<DishCategory> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(DishCategory::getDishId, dishId);
        this.remove(deleteWrapper);
        log.info("删除菜品旧的分类关联 - 菜品ID: {}", dishId);

        // 保存新的分类关联
        if (categoryIds != null && !categoryIds.isEmpty()) {
            saveDishCategories(dishId, categoryIds);
        }
    }

    @Override
    public List<Long> getCategoryIdsByDishId(Long dishId) {
        if (dishId == null) {
            return List.of();
        }

        LambdaQueryWrapper<DishCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishCategory::getDishId, dishId)
                .select(DishCategory::getCategoryId);

        return this.list(queryWrapper).stream()
                .map(DishCategory::getCategoryId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteBatchByDishIds(List<Long> dishIds) {
        if (dishIds == null || dishIds.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<DishCategory> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.in(DishCategory::getDishId, dishIds);
        this.remove(deleteWrapper);
        log.info("批量删除菜品分类关联 - 菜品数量: {}", dishIds.size());
    }

    @Override
    public List<Long> getDishIdsByCategoryId(Long categoryId) {
        if (categoryId == null) {
            return List.of();
        }

        LambdaQueryWrapper<DishCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishCategory::getCategoryId, categoryId)
                .select(DishCategory::getDishId);

        return this.list(queryWrapper).stream()
                .map(DishCategory::getDishId)
                .distinct()
                .collect(Collectors.toList());
    }
}
