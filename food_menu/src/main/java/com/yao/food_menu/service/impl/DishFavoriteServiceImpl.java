package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.dto.DishDto;
import com.yao.food_menu.entity.Category;
import com.yao.food_menu.entity.Dish;
import com.yao.food_menu.entity.DishFavorite;
import com.yao.food_menu.mapper.DishFavoriteMapper;
import com.yao.food_menu.service.CategoryService;
import com.yao.food_menu.service.DishFavoriteService;
import com.yao.food_menu.service.DishService;
import com.yao.food_menu.service.OssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DishFavorite Service Implementation
 */
@Service
@Slf4j
public class DishFavoriteServiceImpl extends ServiceImpl<DishFavoriteMapper, DishFavorite> implements DishFavoriteService {

    private static final String DEFAULT_DISH_IMAGE =
            "https://dummyimage.com/800x600/0f172a/ffffff&text=family+dish";

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OssService ossService;

    @Override
    @Transactional
    public boolean addFavorite(Long userId, Long dishId) {
        try {
            // Check if already favorite
            LambdaQueryWrapper<DishFavorite> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DishFavorite::getUserId, userId)
                    .eq(DishFavorite::getDishId, dishId);
            
            DishFavorite existing = this.getOne(queryWrapper);
            if (existing != null) {
                log.info("Dish already favorited: userId={}, dishId={}", userId, dishId);
                return false;
            }

            // Check if dish exists
            Dish dish = dishService.getById(dishId);
            if (dish == null || dish.getDeleted() == 1) {
                log.warn("Dish not found or deleted: dishId={}", dishId);
                return false;
            }

            // Add favorite
            DishFavorite favorite = new DishFavorite();
            favorite.setUserId(userId);
            favorite.setDishId(dishId);
            boolean result = this.save(favorite);
            
            log.info("Add favorite: userId={}, dishId={}, result={}", userId, dishId, result);
            return result;
        } catch (Exception e) {
            log.error("Add favorite failed: userId={}, dishId={}", userId, dishId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean removeFavorite(Long userId, Long dishId) {
        try {
            LambdaQueryWrapper<DishFavorite> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DishFavorite::getUserId, userId)
                    .eq(DishFavorite::getDishId, dishId);
            
            boolean result = this.remove(queryWrapper);
            log.info("Remove favorite: userId={}, dishId={}, result={}", userId, dishId, result);
            return result;
        } catch (Exception e) {
            log.error("Remove favorite failed: userId={}, dishId={}", userId, dishId, e);
            return false;
        }
    }

    @Override
    public boolean isFavorite(Long userId, Long dishId) {
        LambdaQueryWrapper<DishFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFavorite::getUserId, userId)
                .eq(DishFavorite::getDishId, dishId);
        
        return this.count(queryWrapper) > 0;
    }

    @Override
    public Set<Long> getFavoriteDishIds(Long userId, List<Long> dishIds) {
        if (dishIds == null || dishIds.isEmpty()) {
            return new HashSet<>();
        }

        LambdaQueryWrapper<DishFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFavorite::getUserId, userId)
                .in(DishFavorite::getDishId, dishIds);
        
        List<DishFavorite> favorites = this.list(queryWrapper);
        return favorites.stream()
                .map(DishFavorite::getDishId)
                .collect(Collectors.toSet());
    }

    @Override
    public List<DishDto> getFavoriteDishes(Long userId) {
        // Get favorite dish IDs
        LambdaQueryWrapper<DishFavorite> favoriteQuery = new LambdaQueryWrapper<>();
        favoriteQuery.eq(DishFavorite::getUserId, userId)
                .orderByDesc(DishFavorite::getCreateTime);
        
        List<DishFavorite> favorites = this.list(favoriteQuery);
        
        if (favorites.isEmpty()) {
            return List.of();
        }

        // Get dish IDs
        List<Long> dishIds = favorites.stream()
                .map(DishFavorite::getDishId)
                .collect(Collectors.toList());

        // Get dishes
        LambdaQueryWrapper<Dish> dishQuery = new LambdaQueryWrapper<>();
        dishQuery.in(Dish::getId, dishIds)
                .eq(Dish::getDeleted, 0);
        
        List<Dish> dishes = dishService.list(dishQuery);

        // Convert to DTO and maintain order
        return dishes.stream().map(dish -> {
            if (dish == null) {
                return null;
            }
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish, dishDto);
            
            // Set category name
            Category category = categoryService.getById(dish.getCategoryId());
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }

            enrichDishImage(dishDto);
            
            return dishDto;
        }).filter(dto -> dto != null).collect(Collectors.toList());
    }

    @Override
    public Page<DishDto> getFavoriteDishesPage(Long userId, int page, int pageSize) {
        // Get favorite dish IDs with pagination
        Page<DishFavorite> favoritePage = new Page<>(page, pageSize);
        LambdaQueryWrapper<DishFavorite> favoriteQuery = new LambdaQueryWrapper<>();
        favoriteQuery.eq(DishFavorite::getUserId, userId)
                .orderByDesc(DishFavorite::getCreateTime);
        
        Page<DishFavorite> favoritePageResult = this.page(favoritePage, favoriteQuery);
        
        if (favoritePageResult.getRecords().isEmpty()) {
            return new Page<>(page, pageSize);
        }

        // Get dish IDs
        List<Long> dishIds = favoritePageResult.getRecords().stream()
                .map(DishFavorite::getDishId)
                .collect(Collectors.toList());

        // Get dishes
        LambdaQueryWrapper<Dish> dishQuery = new LambdaQueryWrapper<>();
        dishQuery.in(Dish::getId, dishIds)
                .eq(Dish::getDeleted, 0);
        
        List<Dish> dishes = dishService.list(dishQuery);

        // Convert to DTO
        List<DishDto> dishDtos = dishes.stream().map(dish -> {
            if (dish == null) {
                return null;
            }
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish, dishDto);
            
            // Set category name
            Category category = categoryService.getById(dish.getCategoryId());
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }

            enrichDishImage(dishDto);
            
            return dishDto;
        }).filter(dto -> dto != null).collect(Collectors.toList());

        // Create result page
        Page<DishDto> resultPage = new Page<>(page, pageSize);
        BeanUtils.copyProperties(favoritePageResult, resultPage, "records");
        resultPage.setRecords(dishDtos);
        
        return resultPage;
    }

    /**
     * Convert stored OSS object key to presigned URL so frontend can正确显示图片.
     */
    private void enrichDishImage(DishDto dishDto) {
        if (dishDto == null) {
            return;
        }
        String image = dishDto.getImage();
        if (!StringUtils.hasText(image)) {
            dishDto.setImage(DEFAULT_DISH_IMAGE);
            return;
        }
        if (image.startsWith("http://") || image.startsWith("https://")) {
            return;
        }
        try {
            String presignedUrl = ossService.generatePresignedUrl(image);
            dishDto.setImage(presignedUrl);
        } catch (Exception e) {
            log.warn("生成收藏菜品图片URL失败: {}", image, e);
            dishDto.setImage(DEFAULT_DISH_IMAGE);
        }
    }
}

