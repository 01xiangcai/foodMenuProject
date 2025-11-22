package com.yao.food_menu.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yao.food_menu.entity.OrderItem;
import com.yao.food_menu.entity.Orders;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * Orders DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrdersDto extends Orders {

    // Order items
    @JsonProperty("orderItems")
    private List<OrderItem> orderItems;
}
