package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.dto.DishDto;
import com.yao.food_menu.entity.DishFavorite;

import java.util.List;
import java.util.Set;

/**
 * DishFavorite Service Interface
 */
public interface DishFavoriteService extends IService<DishFavorite> {

    /**
     * Add favorite
     * @param userId User ID
     * @param dishId Dish ID
     * @return true if success, false if already favorite
     */
    boolean addFavorite(Long userId, Long dishId);

    /**
     * Remove favorite
     * @param userId User ID
     * @param dishId Dish ID
     * @return true if success, false if not favorite
     */
    boolean removeFavorite(Long userId, Long dishId);

    /**
     * Check if dish is favorite
     * @param userId User ID
     * @param dishId Dish ID
     * @return true if favorite, false otherwise
     */
    boolean isFavorite(Long userId, Long dishId);

    /**
     * Get favorite dish IDs for batch check
     * @param userId User ID
     * @param dishIds Dish ID list
     * @return Set of favorite dish IDs
     */
    Set<Long> getFavoriteDishIds(Long userId, List<Long> dishIds);

    /**
     * Get favorite dishes list with details
     * @param userId User ID
     * @return List of favorite dishes
     */
    List<DishDto> getFavoriteDishes(Long userId);

    /**
     * Get favorite dishes page
     * @param userId User ID
     * @param page Page number
     * @param pageSize Page size
     * @return Page of favorite dishes
     */
    Page<DishDto> getFavoriteDishesPage(Long userId, int page, int pageSize);
}

