package com.yao.food_menu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yao.food_menu.common.Result;
import com.yao.food_menu.common.util.JwtUtil;
import com.yao.food_menu.entity.DailyMealOrder;
import com.yao.food_menu.entity.OrderItem;
import com.yao.food_menu.entity.Orders;
import com.yao.food_menu.entity.User;
import com.yao.food_menu.entity.WxUser;
import com.yao.food_menu.service.DailyMealOrderService;
import com.yao.food_menu.service.OrderItemService;
import com.yao.food_menu.service.UserService;
import com.yao.food_menu.service.WxUserService;
import com.yao.food_menu.service.OssService;
import com.yao.food_menu.mapper.OrdersMapper;
import com.yao.food_menu.common.config.LocalStorageProperties;
import com.yao.food_menu.common.config.FileStorageProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin端大订单Controller
 */
@Tag(name = "Admin大订单统计", description = "Admin端大订单统计查询")
@RestController
@RequestMapping("/admin/daily-meal-order")
@Slf4j
public class AdminDailyMealOrderController {

    private static final String DEFAULT_AVATAR = "https://dummyimage.com/100x100/ccc/fff&text=User";
    private static final String DEFAULT_DISH_IMAGE = "https://dummyimage.com/100x100/e2e8f0/94a3b8&text=No+Image";

    @Autowired
    private DailyMealOrderService dailyMealOrderService;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private WxUserService wxUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private LocalStorageProperties localStorageProperties;

    @Autowired
    private FileStorageProperties fileStorageProperties;

    @Autowired
    private com.yao.food_menu.service.OrdersService ordersService;

    @Autowired
    private OssService ossService;

    /**
     * 分页查询大订单
     */
    @Operation(summary = "分页查询大订单", description = "分页查询大订单列表")
    @GetMapping
    public Result<IPage<DailyMealOrder>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String mealPeriod,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long familyId,
            @RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = jwtUtil.getUserId(token);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户信息不存在");
            }

            // 构建查询条件
            LambdaQueryWrapper<DailyMealOrder> wrapper = new LambdaQueryWrapper<>();

            // 非超级管理员只能查看自己家庭的数据
            if (user.getRole() != 2 && user.getFamilyId() != null) {
                wrapper.eq(DailyMealOrder::getFamilyId, user.getFamilyId());
            }

            if (startDate != null) {
                wrapper.ge(DailyMealOrder::getOrderDate, LocalDate.parse(startDate));
            }
            if (endDate != null) {
                wrapper.le(DailyMealOrder::getOrderDate, LocalDate.parse(endDate));
            }
            if (mealPeriod != null && !mealPeriod.isEmpty()) {
                wrapper.eq(DailyMealOrder::getMealPeriod, mealPeriod);
            }
            if (status != null) {
                wrapper.eq(DailyMealOrder::getStatus, status);
            }

            // 超级管理员且指定了家庭ID
            if (user.getRole() == 2 && familyId != null) {
                wrapper.eq(DailyMealOrder::getFamilyId, familyId);
            }

            wrapper.orderByDesc(DailyMealOrder::getOrderDate);
            wrapper.orderByAsc(DailyMealOrder::getMealPeriod);

            Page<DailyMealOrder> pageParam = new Page<>(page, pageSize);
            IPage<DailyMealOrder> result = dailyMealOrderService.page(pageParam, wrapper);

            return Result.success(result);
        } catch (Exception e) {
            log.error("查询大订单失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取大订单详情
     */
    @Operation(summary = "获取大订单详情", description = "获取大订单详情,包含成员订单")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = jwtUtil.getUserId(token);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户信息不存在");
            }

            // 获取大订单
            DailyMealOrder dailyMealOrder = dailyMealOrderService.getById(id);
            if (dailyMealOrder == null) {
                return Result.error("大订单不存在");
            }

            // 权限验证:非超级管理员只能查看自己家庭的数据
            if (user.getRole() != 2 && !dailyMealOrder.getFamilyId().equals(user.getFamilyId())) {
                return Result.error("无权限查看");
            }

            // 查询该大订单下的所有小订单
            LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Orders::getDailyMealOrderId, id);
            wrapper.ne(Orders::getStatus, Orders.STATUS_CANCELLED);
            wrapper.orderByAsc(Orders::getCreateTime);
            List<Orders> ordersList = ordersMapper.selectList(wrapper);

            // 组装返回数据
            List<Map<String, Object>> memberOrders = new ArrayList<>();
            for (Orders order : ordersList) {
                Map<String, Object> memberOrder = new HashMap<>();
                memberOrder.put("orderId", order.getId());
                memberOrder.put("orderNumber", order.getOrderNumber());
                memberOrder.put("totalAmount", order.getTotalAmount());
                memberOrder.put("createTime", order.getCreateTime());
                memberOrder.put("isLateOrder", order.getIsLateOrder());
                memberOrder.put("lateOrderStatus", order.getLateOrderStatus());
                memberOrder.put("status", order.getStatus());

                // 查询用户信息
                WxUser member = wxUserService.getById(order.getUserId());
                if (member != null) {
                    memberOrder.put("userId", member.getId());
                    memberOrder.put("nickname", member.getNickname());
                    // 处理头像URL
                    String avatar = member.getAvatar();
                    memberOrder.put("avatar", processImageUrl(avatar, DEFAULT_AVATAR));
                }

                // 查询订单项
                LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
                itemWrapper.eq(OrderItem::getOrderId, order.getId());
                // 如果大订单已确认,只显示已发布的菜品项
                if (dailyMealOrder.getStatus() == DailyMealOrder.STATUS_CONFIRMED) {
                    itemWrapper.eq(OrderItem::getIsPublished, 1);
                }
                List<OrderItem> items = orderItemService.list(itemWrapper);

                // 处理菜品图片URL
                List<Map<String, Object>> processedItems = new ArrayList<>();
                for (OrderItem item : items) {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("id", item.getId());
                    itemMap.put("dishId", item.getDishId());
                    itemMap.put("dishName", item.getDishName());
                    itemMap.put("price", item.getPrice());
                    itemMap.put("quantity", item.getQuantity());
                    itemMap.put("subtotal", item.getSubtotal());
                    itemMap.put("isPublished", item.getIsPublished());
                    // 处理菜品图片URL
                    itemMap.put("dishImage", processImageUrl(item.getDishImage(), DEFAULT_DISH_IMAGE));
                    processedItems.add(itemMap);
                }
                memberOrder.put("items", processedItems);

                memberOrders.add(memberOrder);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("dailyMealOrder", dailyMealOrder);
            result.put("memberOrders", memberOrders);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取大订单详情失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 审核迟到订单
     */
    @Operation(summary = "审核迟到订单", description = "管理员审核迟到订单,接受或拒绝")
    @PostMapping("/review-late-order/{orderId}")
    public Result<String> reviewLateOrder(
            @PathVariable Long orderId,
            @RequestParam Integer action,
            @RequestBody(required = false) List<Long> dishIds,
            @RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = jwtUtil.getUserId(token);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户信息不存在");
            }

            // 验证管理员权限(家庭管理员或超级管理员)
            if (user.getRole() == null || user.getRole() < 1) {
                return Result.error("无权限操作");
            }

            // 调用Service层审核方法
            ordersService.reviewLateOrder(orderId, action, userId, dishIds);

            String actionText = action == Orders.LATE_STATUS_ACCEPTED ? "接受" : "拒绝";
            return Result.success("审核成功: " + actionText);
        } catch (Exception e) {
            log.error("审核迟到订单失败", e);
            return Result.error("审核失败: " + e.getMessage());
        }
    }

    /**
     * 发布菜单
     */
    @Operation(summary = "发布菜单", description = "确认并发布餐次菜单,支持筛选菜品")
    @PostMapping("/confirm-publish/{id}")
    public Result<String> confirmPublish(
            @PathVariable Long id,
            @RequestBody(required = false) List<Long> dishIds,
            @RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = jwtUtil.getUserId(token);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户信息不存在");
            }

            // 验证管理员权限
            if (user.getRole() == null || user.getRole() < 1) {
                return Result.error("无权限操作");
            }

            dailyMealOrderService.confirmOrder(id, userId, dishIds);
            return Result.success("发布成功");
        } catch (Exception e) {
            log.error("发布菜单失败", e);
            return Result.error("发布失败: " + e.getMessage());
        }
    }

    /**
     * 获取大订单统计信息
     */
    @Operation(summary = "获取大订单统计信息", description = "获取各状态大订单数量及汇总数据")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            @RequestParam(required = false) Long paramFamilyId,
            @RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = jwtUtil.getUserId(token);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户信息不存在");
            }

            Long familyId = null;
            // 非超级管理员只能查看自己家庭的数据
            if (user.getRole() != 2 && user.getFamilyId() != null) {
                familyId = user.getFamilyId();
            } else if (user.getRole() == 2 && paramFamilyId != null) {
                // 超级管理员且筛选特定家庭
                familyId = paramFamilyId;
            }

            Map<String, Object> stats = dailyMealOrderService.getStatistics(familyId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取大订单统计信息失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 处理图片URL，根据存储方式转换为完整URL
     * 
     * @param image        原始图片路径
     * @param defaultImage 默认图片URL
     * @return 完整的图片URL
     */
    private String processImageUrl(String image, String defaultImage) {
        if (!StringUtils.hasText(image)) {
            return defaultImage;
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
                log.warn("Failed to generate presigned URL for image: {}", image, e);
                return defaultImage;
            }
        }
    }
}
