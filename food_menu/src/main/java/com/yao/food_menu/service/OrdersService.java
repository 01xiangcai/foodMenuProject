package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.dto.OrdersDto;
import com.yao.food_menu.entity.Orders;

public interface OrdersService extends IService<Orders> {

    /**
     * Submit order
     */
    void submit(OrdersDto ordersDto);

    /**
     * Submit order payment
     */
    void pay(com.yao.food_menu.dto.PayDto payDto);

    /**
     * Update order status
     */
    void updateStatus(Long id, Integer status);

    /**
     * Get order counts by status
     */
    java.util.Map<Integer, Long> getOrderCounts(Long userId);

    /**
     * Get admin order counts (optionally filtered by familyId)
     */
    java.util.Map<Integer, Long> getAdminOrderCounts(Long familyId);

    /**
     * 审核迟到订单
     */
    void reviewLateOrder(Long orderId, Integer action, Long adminId);
}
