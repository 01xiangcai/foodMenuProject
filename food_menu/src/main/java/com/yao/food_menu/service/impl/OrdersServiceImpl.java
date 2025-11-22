package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.dto.OrdersDto;
import com.yao.food_menu.entity.OrderDetail;
import com.yao.food_menu.entity.Orders;
import com.yao.food_menu.mapper.OrdersMapper;
import com.yao.food_menu.service.OrderDetailService;
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
    private OrderDetailService orderDetailService;

    private static final AtomicLong orderCounter = new AtomicLong(1);

    @Override
    @Transactional
    public void submit(OrdersDto ordersDto) {
        // Generate order number
        String orderNumber = generateOrderNumber();
        ordersDto.setNumber(orderNumber);
        ordersDto.setStatus(1); // Pending payment

        // Calculate total amount
        List<OrderDetail> orderDetails = ordersDto.getOrderDetails();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderDetail detail : orderDetails) {
            totalAmount = totalAmount.add(detail.getAmount().multiply(new BigDecimal(detail.getNumber())));
        }
        ordersDto.setAmount(totalAmount);

        // Save order
        this.save(ordersDto);

        // Save order details
        Long orderId = ordersDto.getId();
        for (OrderDetail detail : orderDetails) {
            detail.setOrderId(orderId);
        }
        orderDetailService.saveBatch(orderDetails);

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
     * Format: yyyyMMddHHmmss + 6-digit sequence
     */
    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long sequence = orderCounter.getAndIncrement() % 1000000;
        return timestamp + String.format("%06d", sequence);
    }
}
