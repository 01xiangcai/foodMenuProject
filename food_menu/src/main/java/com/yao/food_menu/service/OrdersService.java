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
     * Update order status
     */
    void updateStatus(Long id, Integer status);
}
