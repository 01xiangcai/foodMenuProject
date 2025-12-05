package com.yao.food_menu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yao.food_menu.common.Result;
import com.yao.food_menu.common.util.JwtUtil;
import com.yao.food_menu.dto.DishDto;
import com.yao.food_menu.service.DishFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Favorite Controller
 */
@Tag(name = "收藏管理", description = "菜品收藏的增删改查")
@RestController
@RequestMapping("/favorite")
@Slf4j
public class FavoriteController {

    @Autowired
    private DishFavoriteService dishFavoriteService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Add favorite
     */
    @Operation(summary = "添加收藏", description = "收藏指定菜品")
    @PostMapping("/add")
    public Result<String> addFavorite(@RequestBody Map<String, Long> request, @RequestHeader("Authorization") String token) {
        log.info("Add favorite: request={}", request);
        
        try {
            // Parse token
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserId(token);
            
            Long dishId = request.get("dishId");
            if (dishId == null) {
                return Result.error("菜品ID不能为空");
            }
            
            boolean result = dishFavoriteService.addFavorite(userId, dishId);
            if (result) {
                return Result.success("收藏成功");
            } else {
                return Result.error("收藏失败，可能已收藏或菜品不存在");
            }
        } catch (Exception e) {
            log.error("Add favorite failed", e);
            return Result.error("收藏失败: " + e.getMessage());
        }
    }

    /**
     * Remove favorite
     */
    @Operation(summary = "取消收藏", description = "取消收藏指定菜品")
    @DeleteMapping("/remove/{dishId}")
    public Result<String> removeFavorite(@PathVariable Long dishId, @RequestHeader("Authorization") String token) {
        log.info("Remove favorite: dishId={}", dishId);
        
        try {
            // Parse token
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserId(token);
            
            boolean result = dishFavoriteService.removeFavorite(userId, dishId);
            if (result) {
                return Result.success("取消收藏成功");
            } else {
                return Result.error("取消收藏失败，可能未收藏");
            }
        } catch (Exception e) {
            log.error("Remove favorite failed", e);
            return Result.error("取消收藏失败: " + e.getMessage());
        }
    }

    /**
     * Check favorite status
     */
    @Operation(summary = "检查收藏状态", description = "检查指定菜品是否已收藏")
    @GetMapping("/check/{dishId}")
    public Result<Boolean> checkFavorite(@PathVariable Long dishId, @RequestHeader("Authorization") String token) {
        log.info("Check favorite: dishId={}", dishId);
        
        try {
            // Parse token
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserId(token);
            
            boolean isFavorite = dishFavoriteService.isFavorite(userId, dishId);
            return Result.success(isFavorite);
        } catch (Exception e) {
            log.error("Check favorite failed", e);
            return Result.error("检查收藏状态失败: " + e.getMessage());
        }
    }

    /**
     * Batch check favorite status
     */
    @Operation(summary = "批量检查收藏状态", description = "批量检查多个菜品是否已收藏")
    @PostMapping("/check/batch")
    public Result<Set<Long>> batchCheckFavorite(@RequestBody Map<String, List<Long>> request, @RequestHeader("Authorization") String token) {
        log.info("Batch check favorite: request={}", request);
        
        try {
            // Parse token
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserId(token);
            
            List<Long> dishIds = request.get("dishIds");
            if (dishIds == null || dishIds.isEmpty()) {
                return Result.success(Set.of());
            }
            
            Set<Long> favoriteDishIds = dishFavoriteService.getFavoriteDishIds(userId, dishIds);
            return Result.success(favoriteDishIds);
        } catch (Exception e) {
            log.error("Batch check favorite failed", e);
            return Result.error("批量检查收藏状态失败: " + e.getMessage());
        }
    }

    /**
     * Get favorite dishes list
     */
    @Operation(summary = "获取收藏列表", description = "获取当前用户的所有收藏菜品")
    @GetMapping("/list")
    public Result<List<DishDto>> getFavoriteList(@RequestHeader("Authorization") String token) {
        log.info("Get favorite list");
        
        try {
            // Parse token
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserId(token);
            
            List<DishDto> favorites = dishFavoriteService.getFavoriteDishes(userId);
            return Result.success(favorites);
        } catch (Exception e) {
            log.error("Get favorite list failed", e);
            return Result.error("获取收藏列表失败: " + e.getMessage());
        }
    }

    /**
     * Get favorite dishes page
     */
    @Operation(summary = "分页查询收藏列表", description = "分页查询当前用户的收藏菜品")
    @GetMapping("/page")
    public Result<Page<DishDto>> getFavoritePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestHeader("Authorization") String token) {
        log.info("Get favorite page: page={}, pageSize={}", page, pageSize);
        
        try {
            // Parse token
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserId(token);
            
            Page<DishDto> favoritePage = dishFavoriteService.getFavoriteDishesPage(userId, page, pageSize);
            return Result.success(favoritePage);
        } catch (Exception e) {
            log.error("Get favorite page failed", e);
            return Result.error("获取收藏列表失败: " + e.getMessage());
        }
    }
}

