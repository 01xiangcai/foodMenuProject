package com.yao.food_menu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yao.food_menu.common.Result;
import com.yao.food_menu.common.util.JwtUtil;
import com.yao.food_menu.dto.OrdersDto;
import com.yao.food_menu.entity.OrderDetail;
import com.yao.food_menu.entity.Orders;
import com.yao.food_menu.service.OrderDetailService;
import com.yao.food_menu.service.OrdersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Orders Controller
 */
@Tag(name = "订单管理", description = "订单提交、查询、状态管理")
@RestController
@RequestMapping("/order")
@Slf4j
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * Submit order
     */
    @Operation(summary = "提交订单", description = "提交订单,自动生成订单号并计算总金额")
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody OrdersDto ordersDto, @RequestHeader("Authorization") String token) {
        log.info("Submit order: {}", ordersDto);

        try {
            // Remove "Bearer " prefix if exists
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = JwtUtil.getUserId(token);
            ordersDto.setUserId(userId);

            ordersService.submit(ordersDto);
            return Result.success("Order submitted successfully");
        } catch (Exception e) {
            log.error("Submit order failed: {}", e.getMessage());
            return Result.error("Submit order failed: " + e.getMessage());
        }
    }

    /**
     * Query orders by page
     */
    @Operation(summary = "分页查询订单", description = "查询当前用户的订单列表")
    @GetMapping("/page")
    public Result<Page<Orders>> page(int page, int pageSize, @RequestHeader("Authorization") String token) {
        log.info("Query orders: page={}, pageSize={}", page, pageSize);

        try {
            // Remove "Bearer " prefix if exists
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = JwtUtil.getUserId(token);

            Page<Orders> pageInfo = new Page<>(page, pageSize);
            LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Orders::getUserId, userId);
            queryWrapper.orderByDesc(Orders::getCreateTime);

            ordersService.page(pageInfo, queryWrapper);
            return Result.success(pageInfo);
        } catch (Exception e) {
            log.error("Query orders failed: {}", e.getMessage());
            return Result.error("Query failed");
        }
    }

    /**
     * Query order details by order id
     */
    @Operation(summary = "查询订单详情", description = "根据订单ID查询订单及订单明细")
    @GetMapping("/{id}")
    public Result<OrdersDto> getById(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        log.info("Query order details: id={}", id);

        try {
            // Remove "Bearer " prefix if exists
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = JwtUtil.getUserId(token);

            Orders orders = ordersService.getById(id);
            if (orders == null) {
                return Result.error("Order not found");
            }

            // Verify order belongs to current user
            if (!orders.getUserId().equals(userId)) {
                return Result.error("Access denied");
            }

            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(orders, ordersDto);

            // Query order details
            LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderDetail::getOrderId, id);
            List<OrderDetail> orderDetails = orderDetailService.list(queryWrapper);
            ordersDto.setOrderDetails(orderDetails);

            return Result.success(ordersDto);
        } catch (Exception e) {
            log.error("Query order details failed: {}", e.getMessage());
            return Result.error("Query failed");
        }
    }

    /**
     * Update order status
     */
    @Operation(summary = "更新订单状态", description = "更新订单状态:1-待支付,2-已支付,3-已完成,4-已取消")
    @PutMapping("/status")
    public Result<String> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        log.info("Update order status: id={}, status={}", id, status);

        try {
            ordersService.updateStatus(id, status);
            return Result.success("Order status updated successfully");
        } catch (Exception e) {
            log.error("Update order status failed: {}", e.getMessage());
            return Result.error("Update failed: " + e.getMessage());
        }
    }
}
