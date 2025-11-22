package com.yao.food_menu.dto;

import com.yao.food_menu.entity.OrderDetail;
import com.yao.food_menu.entity.Orders;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * Orders DTO
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OrdersDto extends Orders {

    // Order details
    private List<OrderDetail> orderDetails;
}
