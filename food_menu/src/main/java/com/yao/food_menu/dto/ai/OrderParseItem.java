package com.yao.food_menu.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单解析结果项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderParseItem {

    /**
     * 菜品名称
     */
    private String dishName;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 匹配到的菜品ID(如果找到)
     */
    private Long dishId;
}
