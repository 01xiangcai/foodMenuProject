package com.yao.food_menu.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 菜品推荐结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DishRecommendation {

    /**
     * 菜品ID
     */
    private Long dishId;

    /**
     * 菜品名称
     */
    private String dishName;

    /**
     * 推荐理由
     */
    private String reason;

    /**
     * 推荐分数(0-100)
     */
    private Integer score;
}
