package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.dto.OrdersDto;
import com.yao.food_menu.entity.Dish;
import com.yao.food_menu.entity.OrderItem;
import com.yao.food_menu.entity.Orders;
import com.yao.food_menu.mapper.OrdersMapper;
import com.yao.food_menu.service.DishService;
import com.yao.food_menu.service.OrderItemService;
import com.yao.food_menu.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private DishService dishService;

    private static final AtomicLong orderCounter = new AtomicLong(1);

    @Override
    @Transactional
    public void submit(OrdersDto ordersDto) {
        log.info("Received order DTO: {}", ordersDto);

        // Validate order items
        List<OrderItem> orderItems = ordersDto.getOrderItems();
        if (orderItems == null || orderItems.isEmpty()) {
            throw new RuntimeException("订单明细不能为空");
        }

        // Generate order number
        String orderNumber = generateOrderNumber();
        ordersDto.setOrderNumber(orderNumber);
        ordersDto.setStatus(0); // Pending (待接单)

        // Calculate total amount and populate dish info
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            // Query dish info and populate order item
            Dish dish = dishService.getById(item.getDishId());
            if (dish != null) {
                item.setDishName(dish.getName());
                item.setDishImage(dish.getImage());
                item.setPrice(dish.getPrice());
            }

            BigDecimal subtotal = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
            item.setSubtotal(subtotal);
            totalAmount = totalAmount.add(subtotal);
        }
        ordersDto.setTotalAmount(totalAmount);

        // Save order
        this.save(ordersDto);

        // Save order items
        Long orderId = ordersDto.getId();
        for (OrderItem item : orderItems) {
            item.setOrderId(orderId);
        }
        orderItemService.saveBatch(orderItems);

        log.info("Order submitted: {}", orderNumber);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Orders orders = this.getById(id);
        if (orders == null) {
            throw new RuntimeException("Order not found");
        }

        orders.setStatus(status);
        this.updateById(orders);

        log.info("Order status updated: {} -> {}", id, status);
    }

    /**
     * Generate order number
     * Format: FM + yyyyMMddHHmmss + 4-digit random
     */
    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long sequence = orderCounter.getAndIncrement() % 10000;
        return "FM" + timestamp + String.format("%04d", sequence);
    }
}
