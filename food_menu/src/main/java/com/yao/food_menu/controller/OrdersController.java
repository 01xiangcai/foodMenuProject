package com.yao.food_menu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yao.food_menu.common.Result;
import com.yao.food_menu.common.util.JwtUtil;
import com.yao.food_menu.dto.OrdersDto;
import com.yao.food_menu.entity.OrderItem;
import com.yao.food_menu.entity.Orders;
import com.yao.food_menu.entity.WxUser;
import com.yao.food_menu.service.OrderItemService;
import com.yao.food_menu.service.OrdersService;
import com.yao.food_menu.service.OssService;
import com.yao.food_menu.service.WxUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Orders Controller
 */
@Tag(name = "订单管理", description = "订单提交、查询、状态管理")
@RestController
@RequestMapping("/order")
@Slf4j
public class OrdersController {

    private static final String DEFAULT_DISH_IMAGE = "https://dummyimage.com/800x600/0f172a/ffffff&text=family+dish";

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private WxUserService wxUserService;

    @Autowired
    private OssService ossService;

    /**
     * Submit order
     */
    @Operation(summary = "提交订单", description = "提交订单,自动生成订单号并计算总金额")
    @PostMapping("/submit")
    public Result<Long> submit(@RequestBody OrdersDto ordersDto, @RequestHeader("Authorization") String token) {
        log.info("Submit order: {}", ordersDto);

        try {
            // Remove "Bearer " prefix if exists
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = JwtUtil.getUserId(token);
            ordersDto.setUserId(userId);

            ordersService.submit(ordersDto);

            // Return created order id for frontend navigation
            return Result.success(ordersDto.getId());
        } catch (Exception e) {
            log.error("Submit order failed: {}", e.getMessage());
            return Result.error("Submit order failed: " + e.getMessage());
        }
    }

    /**
     * Query orders by page
     */
    @Operation(summary = "分页查询订单", description = "查询当前用户的订单列表，包含订单明细")
    @GetMapping("/page")
    public Result<Page<OrdersDto>> page(int page, int pageSize,
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("Query orders: page={}, pageSize={}", page, pageSize);

        try {
            Page<Orders> pageInfo = new Page<>(page, pageSize);
            LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

            // Admin panel should see all orders - no userId filtering
            queryWrapper.orderByDesc(Orders::getCreateTime);

            ordersService.page(pageInfo, queryWrapper);

            // Convert to DTO and load order items + user info
            Page<OrdersDto> dtoPage = new Page<>();
            BeanUtils.copyProperties(pageInfo, dtoPage);

            List<OrdersDto> dtoList = pageInfo.getRecords().stream().map(order -> {
                OrdersDto dto = new OrdersDto();
                BeanUtils.copyProperties(order, dto);

                // Load order items
                LambdaQueryWrapper<OrderItem> itemQueryWrapper = new LambdaQueryWrapper<>();
                itemQueryWrapper.eq(OrderItem::getOrderId, order.getId());
                List<OrderItem> orderItems = orderItemService.list(itemQueryWrapper);
                dto.setOrderItems(orderItems != null ? orderItems : new java.util.ArrayList<>());
                enrichOrderItemImages(dto.getOrderItems());

                // Load user information
                WxUser wxUser = wxUserService.getById(order.getUserId());
                if (wxUser != null) {
                    dto.setUserNickname(wxUser.getNickname());
                    dto.setUserPhone(wxUser.getPhone());
                    // Generate presigned URL for avatar
                    if (StringUtils.hasText(wxUser.getAvatar())) {
                        try {
                            String presignedUrl = ossService.generatePresignedUrl(wxUser.getAvatar());
                            dto.setUserAvatar(presignedUrl);
                        } catch (Exception e) {
                            log.warn("Failed to generate presigned URL for avatar: {}", wxUser.getAvatar(), e);
                            dto.setUserAvatar(wxUser.getAvatar());
                        }
                    } else {
                        dto.setUserAvatar(wxUser.getAvatar());
                    }
                }

                return dto;
            }).collect(Collectors.toList());

            dtoPage.setRecords(dtoList);

            return Result.success(dtoPage);
        } catch (Exception e) {
            log.error("Query orders failed: {}", e.getMessage());
            return Result.error("Query failed");
        }
    }

    /**
     * Query current user's orders (for mini program)
     */
    @Operation(summary = "查询我的订单", description = "小程序用户查询自己的订单列表")
    @GetMapping("/my")
    public Result<Page<OrdersDto>> myOrders(int page, int pageSize,
            @RequestHeader("Authorization") String token) {
        log.info("Query my orders: page={}, pageSize={}", page, pageSize);

        try {
            // Remove "Bearer " prefix if exists
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = JwtUtil.getUserId(token);
            log.info("Query orders for wx_user: {}", userId);

            Page<Orders> pageInfo = new Page<>(page, pageSize);
            LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

            // Filter by current user's ID (wx_user)
            queryWrapper.eq(Orders::getUserId, userId);
            queryWrapper.orderByDesc(Orders::getCreateTime);

            ordersService.page(pageInfo, queryWrapper);

            // Convert to DTO and load order items + user info
            Page<OrdersDto> dtoPage = new Page<>();
            BeanUtils.copyProperties(pageInfo, dtoPage);

            List<OrdersDto> dtoList = pageInfo.getRecords().stream().map(order -> {
                OrdersDto dto = new OrdersDto();
                BeanUtils.copyProperties(order, dto);

                // Load order items
                LambdaQueryWrapper<OrderItem> itemQueryWrapper = new LambdaQueryWrapper<>();
                itemQueryWrapper.eq(OrderItem::getOrderId, order.getId());
                List<OrderItem> orderItems = orderItemService.list(itemQueryWrapper);
                dto.setOrderItems(orderItems != null ? orderItems : new java.util.ArrayList<>());
                enrichOrderItemImages(dto.getOrderItems());

                // Load user information
                WxUser wxUser = wxUserService.getById(order.getUserId());
                if (wxUser != null) {
                    dto.setUserNickname(wxUser.getNickname());
                    dto.setUserPhone(wxUser.getPhone());
                    // Generate presigned URL for avatar
                    if (StringUtils.hasText(wxUser.getAvatar())) {
                        try {
                            String presignedUrl = ossService.generatePresignedUrl(wxUser.getAvatar());
                            dto.setUserAvatar(presignedUrl);
                        } catch (Exception e) {
                            log.warn("Failed to generate presigned URL for avatar: {}", wxUser.getAvatar(), e);
                            dto.setUserAvatar(wxUser.getAvatar());
                        }
                    } else {
                        dto.setUserAvatar(wxUser.getAvatar());
                    }
                }

                return dto;
            }).collect(Collectors.toList());

            dtoPage.setRecords(dtoList);

            return Result.success(dtoPage);
        } catch (Exception e) {
            log.error("Query my orders failed: {}", e.getMessage());
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

            // Query order items
            LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderItem::getOrderId, id);
            List<OrderItem> orderItems = orderItemService.list(queryWrapper);
            ordersDto.setOrderItems(orderItems);
            enrichOrderItemImages(ordersDto.getOrderItems());

            return Result.success(ordersDto);
        } catch (Exception e) {
            log.error("Query order details failed: {}", e.getMessage());
            return Result.error("Query failed");
        }
    }

    /**
     * Update order status
     */
    @Operation(summary = "更新订单状态", description = "更新订单状态:0-待接单,1-准备中,2-配送中,3-已完成,4-已取消")
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

    /**
     * Ensure each order item carries a valid presigned image URL before returning
     * to the client.
     * This prevents frontend issues such as expired OSS URLs or missing domain
     * prefixes.
     */
    private void enrichOrderItemImages(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return;
        }
        for (OrderItem item : orderItems) {
            if (item == null) {
                continue;
            }
            String image = item.getDishImage();
            if (!StringUtils.hasText(image)) {
                item.setDishImage(DEFAULT_DISH_IMAGE);
                continue;
            }
            if (!image.startsWith("http://") && !image.startsWith("https://")) {
                try {
                    String presignedUrl = ossService.generatePresignedUrl(image);
                    item.setDishImage(presignedUrl);
                } catch (Exception e) {
                    log.warn("Failed to generate presigned URL for order item image: {}", image, e);
                    item.setDishImage(DEFAULT_DISH_IMAGE);
                }
            }
        }
    }
}
