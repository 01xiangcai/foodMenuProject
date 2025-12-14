package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.entity.DishCategory;

import java.util.List;

/**
 * 菜品分类关联 Service 接口
 */
public interface DishCategoryService extends IService<DishCategory> {

    /**
     * 保存菜品的分类关联
     * 
     * @param dishId      菜品ID
     * @param categoryIds 分类ID列表
     */
    void saveDishCategories(Long dishId, List<Long> categoryIds);

    /**
     * 更新菜品的分类关联(先删除旧的,再添加新的)
     * 
     * @param dishId      菜品ID
     * @param categoryIds 分类ID列表
     */
    void updateDishCategories(Long dishId, List<Long> categoryIds);

    /**
     * 获取菜品的所有分类ID
     * 
     * @param dishId 菜品ID
     * @return 分类ID列表
     */
    List<Long> getCategoryIdsByDishId(Long dishId);

    /**
     * 根据菜品ID列表批量删除分类关联
     * 
     * @param dishIds 菜品ID列表
     */
    void deleteBatchByDishIds(List<Long> dishIds);

    /**
     * 根据分类ID获取该分类下的所有菜品ID
     * 
     * @param categoryId 分类ID
     * @return 菜品ID列表
     */
    List<Long> getDishIdsByCategoryId(Long categoryId);
}
