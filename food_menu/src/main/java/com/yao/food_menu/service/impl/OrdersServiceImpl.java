package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.common.context.FamilyContext;
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

    @Autowired
    private com.yao.food_menu.service.DishStatisticsService dishStatisticsService;

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

        // 自动设置familyId
        if (ordersDto.getFamilyId() == null) {
            Long familyId = FamilyContext.getFamilyId();
            if (familyId != null) {
                ordersDto.setFamilyId(familyId);
            }
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
                // 优先使用 localImage（本地图片/主图），如果为空则使用 image
                String dishImage = dish.getLocalImage();
                if (dishImage == null || dishImage.trim().isEmpty()) {
                    dishImage = dish.getImage();
                }
                item.setDishImage(dishImage);
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
        Long familyId = ordersDto.getFamilyId();
        for (OrderItem item : orderItems) {
            item.setOrderId(orderId);
            item.setFamilyId(familyId);
        }
        orderItemService.saveBatch(orderItems);

        log.info("Order submitted: {}", orderNumber);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        Orders orders = this.getById(id);
        if (orders == null) {
            throw new RuntimeException("Order not found");
        }

        Integer oldStatus = orders.getStatus();
        orders.setStatus(status);
        
        // 根据状态记录对应的时间
        LocalDateTime now = LocalDateTime.now();
        if (status == 1 && oldStatus != 1) {
            // 接单
            orders.setAcceptTime(now);
        } else if (status == 2 && oldStatus != 2) {
            // 开始配送
            orders.setDeliveryTime(now);
        } else if (status == 3 && oldStatus != 3) {
            // 订单完成
            orders.setCompleteTime(now);
        }
        
        this.updateById(orders);

        log.info("Order status updated: {} -> {}", id, status);

        // 如果订单状态变更为已完成(2或3),更新菜品统计
        if ((status == 2 || status == 3) && !status.equals(oldStatus)) {
            log.info("Order completed, updating dish statistics for order: {}", id);
            try {
                // 查询订单明细
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItem> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                queryWrapper.eq(OrderItem::getOrderId, id);
                List<OrderItem> orderItems = orderItemService.list(queryWrapper);

                // 提取菜品ID列表并更新统计
                List<Long> dishIds = orderItems.stream()
                        .map(OrderItem::getDishId)
                        .distinct()
                        .collect(java.util.stream.Collectors.toList());

                dishStatisticsService.batchIncrementOrderCount(dishIds);
                log.info("Dish statistics updated for {} dishes", dishIds.size());
            } catch (Exception e) {
                log.error("Failed to update dish statistics for order: {}", id, e);
                // 不影响订单状态更新,只记录错误
            }
        }
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
