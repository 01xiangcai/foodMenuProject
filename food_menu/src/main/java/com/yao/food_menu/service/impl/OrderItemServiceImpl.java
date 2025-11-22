package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.entity.OrderItem;
import com.yao.food_menu.mapper.OrderItemMapper;
import com.yao.food_menu.service.OrderItemService;
import org.springframework.stereotype.Service;

/**
 * Order item service implementation
 */
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {
}
