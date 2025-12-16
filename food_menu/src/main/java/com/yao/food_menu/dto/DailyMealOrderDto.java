package com.yao.food_menu.dto;

import com.yao.food_menu.entity.DailyMealOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 大订单DTO
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DailyMealOrderDto extends DailyMealOrder {

    // 成员订单列表
    private List<Map<String, Object>> memberOrders;
}
