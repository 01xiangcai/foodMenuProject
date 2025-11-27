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
    @Operation(summary = "分页查询订单", description = "分页查询订单列表，包含订单明细，后台和小程序管理员使用")
    @GetMapping("/page")
    public Result<Page<OrdersDto>> page(int page, int pageSize,
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("Query orders: page={}, pageSize={}", page, pageSize);

        try {
            Page<Orders> pageInfo = new Page<>(page, pageSize);
            LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

            // 后台和小程序管理员默认查看全部订单，不按用户过滤
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

            // Verify order belongs to current user or user is admin
            if (!orders.getUserId().equals(userId) && !wxUserService.isAdmin(userId)) {
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
     * Query all orders (for mini program admin)
     */
    @Operation(summary = "管理员查询所有订单", description = "小程序管理员查看所有订单")
    @GetMapping("/admin")
    public Result<Page<OrdersDto>> adminOrders(int page, int pageSize,
            @RequestHeader("Authorization") String token) {
        log.info("Query admin orders: page={}, pageSize={}", page, pageSize);

        try {
            // Remove "Bearer " prefix if exists
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = JwtUtil.getUserId(token);
            if (!wxUserService.isAdmin(userId)) {
                return Result.error("无权限访问");
            }

            Page<Orders> pageInfo = new Page<>(page, pageSize);
            LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
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
            log.error("Query admin orders failed: {}", e.getMessage());
            return Result.error("Query failed");
        }
    }

    /**
     * Update order status
     */
    @Operation(summary = "更新订单状态", description = "更新订单状态:0-待接单,1-准备中,2-配送中,3-已完成,4-已取消。仅管理员可操作，普通用户仅可取消")
    @PutMapping("/status")
    public Result<String> updateStatus(@RequestParam Long id, @RequestParam Integer status,
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("Update order status: id={}, status={}", id, status);

        try {
            boolean isAdmin = false;
            Long userId = null;

            if (StringUtils.hasText(token)) {
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }
                try {
                    userId = JwtUtil.getUserId(token);
                    // 小程序端管理员
                    if (wxUserService.isAdmin(userId)) {
                        isAdmin = true;
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse token when updating order status: {}", e.getMessage());
                }
            }

            // 如果不是管理员，检查是否为普通用户取消自己的订单
            if (!isAdmin) {
                // 如果没有token（可能是后台管理端），或者有token但不是管理员
                if (token == null) {
                    // 后台管理端请求，默认允许（假设网关已鉴权）
                    log.warn("No token provided when updating order status, assuming admin panel context");
                } else {
                    // 小程序普通用户
                    if (userId == null) {
                        return Result.error("未登录");
                    }

                    // 只能执行取消操作 (status = 4)
                    if (status != 4) {
                        return Result.error("无权限执行此操作");
                    }

                    // 检查订单是否属于当前用户
                    Orders order = ordersService.getById(id);
                    if (order == null) {
                        return Result.error("订单不存在");
                    }
                    if (!order.getUserId().equals(userId)) {
                        return Result.error("无权限修改他人订单");
                    }
                }
            }

            ordersService.updateStatus(id, status);
            return Result.success("Order status updated successfully");
        } catch (Exception e) {
            log.error("Update order status failed: {}", e.getMessage());
            return Result.error("Update failed: " + e.getMessage());
        }
    }

    /**
     * Delete order
     */
    @Operation(summary = "删除订单", description = "删除订单及其明细，仅允许删除已完成或已取消的订单")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("Delete order: id={}", id);

        try {
            boolean isAdmin = false;
            Long userId = null;

            if (StringUtils.hasText(token)) {
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }
                try {
                    userId = JwtUtil.getUserId(token);
                    if (wxUserService.isAdmin(userId)) {
                        isAdmin = true;
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse token: {}", e.getMessage());
                }
            }

            Orders order = ordersService.getById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }

            // 鉴权：如果是后台请求(token为null)默认为管理员，否则检查是否为管理员或订单所有者
            if (!isAdmin) {
                if (token != null && userId != null) {
                    if (!order.getUserId().equals(userId)) {
                        return Result.error("无权限删除他人订单");
                    }
                }
                // 如果是后台管理端请求(token==null)，默认允许
            }

            // 检查状态：只能删除已完成(3)或已取消(4)的订单
            if (order.getStatus() != 3 && order.getStatus() != 4) {
                return Result.error("只能删除已完成或已取消的订单");
            }

            // 删除订单明细
            LambdaQueryWrapper<OrderItem> itemQueryWrapper = new LambdaQueryWrapper<>();
            itemQueryWrapper.eq(OrderItem::getOrderId, id);
            orderItemService.remove(itemQueryWrapper);

            // 删除订单
            ordersService.removeById(id);

            return Result.success("订单删除成功");
        } catch (Exception e) {
            log.error("Delete order failed: {}", e.getMessage());
            return Result.error("删除失败: " + e.getMessage());
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
