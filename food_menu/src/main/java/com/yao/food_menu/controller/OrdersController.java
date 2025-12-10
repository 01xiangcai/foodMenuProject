package com.yao.food_menu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yao.food_menu.common.Result;
import com.yao.food_menu.common.util.JwtUtil;
import com.yao.food_menu.dto.OrdersDto;
import com.yao.food_menu.entity.OrderItem;
import com.yao.food_menu.entity.Orders;
import com.yao.food_menu.entity.WxUser;
import com.yao.food_menu.entity.Dish;
import com.yao.food_menu.entity.User;
import com.yao.food_menu.service.DishService;
import com.yao.food_menu.service.OrderItemService;
import com.yao.food_menu.service.OrdersService;
import com.yao.food_menu.service.OssService;
import com.yao.food_menu.service.UserService;
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
    private UserService userService;

    @Autowired
    private OssService ossService;

    @Autowired
    private DishService dishService;

    @Autowired
    private com.yao.food_menu.common.config.FileStorageProperties fileStorageProperties;

    @Autowired
    private com.yao.food_menu.common.config.LocalStorageProperties localStorageProperties;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Submit order
     */
    @Operation(summary = "提交订单", description = "提交订单,自动生成订单号并计算总金额")
    @com.yao.food_menu.common.annotation.RateLimiter(qps = 3, timeout = 500, message = "订单提交过于频繁，请稍后再试", limitType = com.yao.food_menu.common.annotation.RateLimiter.LimitType.USER)
    @com.yao.food_menu.common.annotation.PreventDuplicateSubmit(interval = 3000, message = "订单已提交")
    @com.yao.food_menu.common.annotation.OperationLog(operationType = com.yao.food_menu.common.annotation.OperationLog.OperationType.INSERT, operationModule = "订单", operationDesc = "提交订单", recordParams = true, recordResult = true)
    @PostMapping("/submit")
    public Result<Long> submit(@RequestBody OrdersDto ordersDto, @RequestHeader("Authorization") String token) {
        log.info("Submit order: {}", ordersDto);

        try {
            // Remove "Bearer " prefix if exists
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = jwtUtil.getUserId(token);
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
     * Order Payment
     */
    @Operation(summary = "订单支付", description = "对已提交的订单进行支付")
    @PostMapping("/pay")
    public Result<String> pay(@RequestBody com.yao.food_menu.dto.PayDto payDto,
            @RequestHeader("Authorization") String token) {
        log.info("Pay order: {}", payDto);

        try {
            // Remove "Bearer " prefix if exists
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            // Verify token checks out (optional: check if user matches order owner, but
            // service layer handles some checks)
            jwtUtil.getUserId(token);

            ordersService.pay(payDto);

            return Result.success("支付成功");
        } catch (Exception e) {
            log.error("Pay order failed: {}", e.getMessage());
            return Result.error("支付失败: " + e.getMessage());
        }
    }

    /**
     * Get order counts
     */
    @Operation(summary = "获取订单数量统计", description = "获取各状态订单数量")
    @GetMapping("/count")
    public Result<java.util.Map<Integer, Long>> getOrderCounts(@RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.getUserId(token);
            java.util.Map<Integer, Long> counts = ordersService.getOrderCounts(userId);
            return Result.success(counts);
        } catch (Exception e) {
            log.error("Get order counts failed: {}", e.getMessage());
            return Result.error("获取统计失败: " + e.getMessage());
        }
    }

    /**
     * Get admin order counts
     */
    @Operation(summary = "管理员获取订单统计", description = "管理员获取各状态订单数量")
    @GetMapping("/admin/count")
    public Result<java.util.Map<Integer, Long>> getAdminOrderCounts(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) Long familyId) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long currentUserId = jwtUtil.getUserId(token);

            // 小程序端管理员是wx_user,需要查询wx_user表
            WxUser wxUser = wxUserService.getById(currentUserId);
            if (wxUser == null) {
                return Result.error("用户不存在");
            }

            // 小程序端管理员只能查看自己家庭的订单统计
            familyId = wxUser.getFamilyId();

            java.util.Map<Integer, Long> counts = ordersService.getAdminOrderCounts(familyId);
            return Result.success(counts);
        } catch (Exception e) {
            log.error("Get admin order counts failed: {}", e.getMessage());
            return Result.error("获取统计失败: " + e.getMessage());
        }
    }

    /**
     * Query orders by page
     */
    @Operation(summary = "分页查询订单", description = "分页查询订单列表，包含订单明细，后台和小程序管理员使用")
    @GetMapping("/page")
    public Result<Page<OrdersDto>> page(int page, int pageSize,
            @RequestParam(value = "familyId", required = false) Long familyId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("Query orders: page={}, pageSize={}, familyId={}, status={}", page, pageSize, familyId, status);

        try {
            Page<Orders> pageInfo = new Page<>(page, pageSize);
            LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

            // 如果指定了家庭ID，按家庭筛选
            if (familyId != null) {
                queryWrapper.eq(Orders::getFamilyId, familyId);
            }

            // 如果指定了状态，按状态筛选
            if (status != null && status != -1) {
                queryWrapper.eq(Orders::getStatus, status);
            }

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
            @RequestParam(value = "status", required = false) Integer status,
            @RequestHeader("Authorization") String token) {
        log.info("Query my orders: page={}, pageSize={}, status={}", page, pageSize, status);

        try {
            // Remove "Bearer " prefix if exists
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = jwtUtil.getUserId(token);
            log.info("Query orders for wx_user: {}", userId);

            Page<Orders> pageInfo = new Page<>(page, pageSize);
            LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

            // Filter by current user's ID (wx_user)
            queryWrapper.eq(Orders::getUserId, userId);

            // 如果指定了状态，按状态筛选
            if (status != null && status != -1) {
                queryWrapper.eq(Orders::getStatus, status);
            }

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
            log.error("Query my orders failed", e);
            return Result.error("Query failed: " + e.toString());
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

            Long userId = jwtUtil.getUserId(token);

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
            @RequestParam(value = "status", required = false) Integer status,
            @RequestHeader("Authorization") String token) {
        log.info("Query admin orders: page={}, pageSize={}, status={}", page, pageSize, status);

        try {
            // Remove "Bearer " prefix if exists
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = jwtUtil.getUserId(token);
            if (!wxUserService.isAdmin(userId)) {
                return Result.error("无权限访问");
            }

            Page<Orders> pageInfo = new Page<>(page, pageSize);
            LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

            // 如果指定了状态，按状态筛选
            if (status != null && status != -1) {
                queryWrapper.eq(Orders::getStatus, status);
            }

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
     * Update order remark
     */
    @Operation(summary = "修改订单备注", description = "修改订单备注，仅待接单状态下可修改")
    @PutMapping("/remark")
    public Result<String> updateRemark(@RequestParam Long id, @RequestParam String remark,
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("Update order remark: id={}, remark={}", id, remark);

        try {
            // Remove "Bearer " prefix if exists
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = jwtUtil.getUserId(token);
            if (userId == null) {
                return Result.error("未登录");
            }

            Orders order = ordersService.getById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }

            // Verify ownership
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权限修改他人订单");
            }

            // Verify status (only 0: Pending Acceptance)
            if (order.getStatus() != 0) {
                return Result.error("当前状态不可修改备注");
            }

            order.setRemark(remark);
            ordersService.updateById(order);

            return Result.success("备注修改成功");
        } catch (Exception e) {
            log.error("Update order remark failed: {}", e.getMessage());
            return Result.error("修改失败: " + e.getMessage());
        }
    }

    /**
     * Update order status
     */
    @Operation(summary = "更新订单状态", description = "更新订单状态:0-待接单,1-准备中,2-配送中,3-已完成,4-已取消。仅管理员可操作，普通用户仅可取消")
    @com.yao.food_menu.common.annotation.OperationLog(operationType = com.yao.food_menu.common.annotation.OperationLog.OperationType.UPDATE, operationModule = "订单", operationDesc = "更新订单状态", recordParams = true)
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
                    userId = jwtUtil.getUserId(token);
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
                    userId = jwtUtil.getUserId(token);
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
     * Also checks dish status to prevent viewing details of discontinued dishes.
     * Priority: Use latest image from dish table, fallback to order item's stored
     * image.
     */
    private void enrichOrderItemImages(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return;
        }
        for (OrderItem item : orderItems) {
            if (item == null) {
                continue;
            }

            String imageToUse = null; // 最终使用的图片路径或URL

            // 优先从菜品表中查询最新图片
            if (item.getDishId() != null) {
                Dish dish = dishService.getById(item.getDishId());
                if (dish != null) {
                    // 设置菜品状态：1-在售, 0-停售
                    item.setDishStatus(dish.getStatus() != null ? dish.getStatus() : 0);

                    // 优先使用菜品表中的最新图片
                    if (fileStorageProperties.isLocal()) {
                        // 本地存储模式：优先使用 localImage
                        if (StringUtils.hasText(dish.getLocalImage())) {
                            imageToUse = dish.getLocalImage();
                        } else if (StringUtils.hasText(dish.getImage())) {
                            imageToUse = dish.getImage();
                        }
                    } else {
                        // OSS存储模式：使用 image 字段
                        if (StringUtils.hasText(dish.getImage())) {
                            imageToUse = dish.getImage();
                        }
                    }
                } else {
                    // 菜品不存在，标记为已下架
                    item.setDishStatus(0);
                }
            } else {
                item.setDishStatus(0);
            }

            // 如果菜品表中没有图片，使用订单项中存储的图片作为兜底
            if (!StringUtils.hasText(imageToUse)) {
                imageToUse = item.getDishImage();
            }

            // 如果还是没有图片，使用默认图片
            if (!StringUtils.hasText(imageToUse)) {
                item.setDishImage(DEFAULT_DISH_IMAGE);
                continue;
            }

            // 处理图片URL：转换为完整URL或预签名URL
            String finalImageUrl = processImageUrl(imageToUse);
            item.setDishImage(finalImageUrl);
        }
    }

    /**
     * 处理图片URL，根据存储方式转换为完整URL或预签名URL
     * 与 DishController 中的逻辑保持一致
     */
    private String processImageUrl(String image) {
        if (!StringUtils.hasText(image)) {
            return DEFAULT_DISH_IMAGE;
        }

        // 如果已经是完整URL，直接返回
        if (image.startsWith("http://") || image.startsWith("https://")) {
            return image;
        }

        // 根据存储方式处理
        if (fileStorageProperties.isLocal()) {
            // 本地存储模式：拼接URL前缀
            String urlPrefix = localStorageProperties.getUrlPrefix();
            if (!urlPrefix.endsWith("/")) {
                urlPrefix += "/";
            }
            // 移除image开头的斜杠
            String localPath = image.startsWith("/") ? image.substring(1) : image;
            return urlPrefix + localPath;
        } else {
            // OSS存储模式：转换为预签名URL
            try {
                return ossService.generatePresignedUrl(image);
            } catch (Exception e) {
                log.warn("Failed to generate presigned URL for order item image: {}", image, e);
                return DEFAULT_DISH_IMAGE;
            }
        }
    }
}
