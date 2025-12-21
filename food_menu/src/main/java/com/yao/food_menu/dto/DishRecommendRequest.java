package com.yao.food_menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 菜品推荐请求DTO
 */
@Data
@Schema(description = "菜品推荐请求")
public class DishRecommendRequest {

    @Schema(description = "餐次(早餐/午餐/晚餐)", example = "午餐")
    private String mealPeriod;

    @Schema(description = "用户偏好描述", example = "清淡少油")
    private String preferences;

    @Schema(description = "推荐数量", example = "3")
    private Integer count = 3;
}
