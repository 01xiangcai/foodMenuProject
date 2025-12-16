package com.yao.food_menu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yao.food_menu.common.Result;
import com.yao.food_menu.common.util.JwtUtil;
import com.yao.food_menu.entity.DailyMealOrder;
import com.yao.food_menu.entity.OrderItem;
import com.yao.food_menu.entity.Orders;
import com.yao.food_menu.entity.WxUser;
import com.yao.food_menu.service.DailyMealOrderService;
import com.yao.food_menu.service.OrderItemService;
import com.yao.food_menu.service.OrdersService;
import com.yao.food_menu.service.WxUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小程序大订单Controller
 */
@Tag(name = "小程序今日菜单", description = "今日菜单大订单管理")
@RestController
@RequestMapping("/uniapp/daily-meal-order")
@Slf4j
public class UniappDailyMealOrderController {

    @Autowired
    private DailyMealOrderService dailyMealOrderService;

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private WxUserService wxUserService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private com.yao.food_menu.common.config.LocalStorageProperties localStorageProperties;

    @Autowired
    private com.yao.food_menu.service.DailyMealPublishItemService dailyMealPublishItemService;

    /**
     * 获取今日菜单列表(早中晚三个卡片数据)
     */
    @Operation(summary = "获取今日菜单列表", description = "获取今天的早中晚三个餐次的大订单信息")
    @GetMapping("/today")
    public Result<List<DailyMealOrder>> getTodayList(@RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = jwtUtil.getUserId(token);
            WxUser wxUser = wxUserService.getById(userId);
            if (wxUser == null || wxUser.getFamilyId() == null) {
                return Result.error("用户信息不完整");
            }

            List<DailyMealOrder> orders = dailyMealOrderService.getByFamilyAndDate(
                    wxUser.getFamilyId(),
                    LocalDate.now());

            return Result.success(orders);
        } catch (Exception e) {
            log.error("获取今日菜单列表失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取大订单详情
     */
    @Operation(summary = "获取大订单详情", description = "获取大订单详情,包含所有家庭成员的小订单")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = jwtUtil.getUserId(token);
            WxUser wxUser = wxUserService.getById(userId);
            if (wxUser == null || wxUser.getFamilyId() == null) {
                return Result.error("用户信息不完整");
            }

            // 获取大订单
            DailyMealOrder dailyMealOrder = dailyMealOrderService.getById(id);
            if (dailyMealOrder == null) {
                return Result.error("大订单不存在");
            }

            // 验证权限:只能查看自己家庭的大订单
            if (!dailyMealOrder.getFamilyId().equals(wxUser.getFamilyId())) {
                return Result.error("无权限查看");
            }

            // 查询该大订单下的所有小订单
            LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Orders::getDailyMealOrderId, id);
            wrapper.ne(Orders::getStatus, Orders.STATUS_CANCELLED); // 排除已取消的订单
            wrapper.orderByAsc(Orders::getCreateTime);
            List<Orders> ordersList = ordersService.list(wrapper);

            // 组装返回数据
            List<Map<String, Object>> memberOrders = new ArrayList<>();
            for (Orders order : ordersList) {
                Map<String, Object> memberOrder = new HashMap<>();
                memberOrder.put("orderId", order.getId());
                memberOrder.put("orderNumber", order.getOrderNumber());
                memberOrder.put("totalAmount", order.getTotalAmount());
                memberOrder.put("createTime", order.getCreateTime());

                // 查询用户信息
                WxUser member = wxUserService.getById(order.getUserId());
                if (member != null) {
                    memberOrder.put("userId", member.getId());
                    memberOrder.put("nickname", member.getNickname());
                    // 处理头像URL
                    String avatar = member.getAvatar();
                    if (avatar != null && !avatar.startsWith("http://") && !avatar.startsWith("https://")) {
                        String urlPrefix = localStorageProperties.getUrlPrefix();
                        if (!urlPrefix.endsWith("/")) {
                            urlPrefix += "/";
                        }
                        avatar = urlPrefix + avatar;
                    }
                    memberOrder.put("avatar", avatar);
                }

                // 根据订单状态决定查询来源
                List<Map<String, Object>> items = new ArrayList<>();

                if (dailyMealOrder.getStatus() == DailyMealOrder.STATUS_CONFIRMED) {
                    // 已确认订单:从发布记录表查询
                    LambdaQueryWrapper<com.yao.food_menu.entity.DailyMealPublishItem> publishWrapper = new LambdaQueryWrapper<>();
                    publishWrapper.eq(com.yao.food_menu.entity.DailyMealPublishItem::getOrderId, order.getId());
                    List<com.yao.food_menu.entity.DailyMealPublishItem> publishItems = dailyMealPublishItemService
                            .list(publishWrapper);

                    for (com.yao.food_menu.entity.DailyMealPublishItem publishItem : publishItems) {
                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put("id", publishItem.getOrderItemId());
                        itemMap.put("dishId", publishItem.getDishId());
                        itemMap.put("dishName", publishItem.getDishName());
                        itemMap.put("quantity", publishItem.getQuantity());
                        itemMap.put("price", publishItem.getPrice());
                        itemMap.put("subtotal", publishItem.getSubtotal());

                        // 处理菜品图片URL
                        String dishImage = publishItem.getDishImage();
                        if (dishImage != null && !dishImage.startsWith("http://")
                                && !dishImage.startsWith("https://")) {
                            String urlPrefix = localStorageProperties.getUrlPrefix();
                            if (!urlPrefix.endsWith("/")) {
                                urlPrefix += "/";
                            }
                            dishImage = urlPrefix + dishImage;
                        }
                        itemMap.put("dishImage", dishImage);

                        items.add(itemMap);
                    }
                } else {
                    // 未确认订单:从原订单项表查询
                    LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
                    itemWrapper.eq(OrderItem::getOrderId, order.getId());
                    List<OrderItem> orderItems = orderItemService.list(itemWrapper);

                    for (OrderItem item : orderItems) {
                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put("id", item.getId());
                        itemMap.put("dishId", item.getDishId());
                        itemMap.put("dishName", item.getDishName());
                        itemMap.put("quantity", item.getQuantity());
                        itemMap.put("price", item.getPrice());
                        itemMap.put("isPublished", item.getIsPublished() != null ? item.getIsPublished() : 0);

                        // 计算小计
                        java.math.BigDecimal subtotal = item.getPrice()
                                .multiply(new java.math.BigDecimal(item.getQuantity()));
                        itemMap.put("subtotal", subtotal);

                        // 处理菜品图片URL
                        String dishImage = item.getDishImage();
                        if (dishImage != null && !dishImage.startsWith("http://")
                                && !dishImage.startsWith("https://")) {
                            String urlPrefix = localStorageProperties.getUrlPrefix();
                            if (!urlPrefix.endsWith("/")) {
                                urlPrefix += "/";
                            }
                            dishImage = urlPrefix + dishImage;
                        }
                        itemMap.put("dishImage", dishImage);

                        items.add(itemMap);
                    }
                }

                memberOrder.put("items", items);

                // 只添加有菜品的订单
                if (!items.isEmpty()) {
                    memberOrders.add(memberOrder);
                }
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
     * 确认发布大订单(仅管理员)
     */
    @Operation(summary = "确认发布大订单", description = "管理员确认发布大订单,可选择性发布菜品,未选中的菜品将被删除")
    @PostMapping("/confirm/{id}")
    public Result<String> confirmOrder(@PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> requestBody,
            @RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = jwtUtil.getUserId(token);
            WxUser wxUser = wxUserService.getById(userId);
            if (wxUser == null || wxUser.getFamilyId() == null) {
                return Result.error("用户信息不完整");
            }

            // 检查是否为管理员
            if (!wxUserService.isAdmin(userId)) {
                return Result.error("无权限操作");
            }

            // 获取大订单
            DailyMealOrder dailyMealOrder = dailyMealOrderService.getById(id);
            if (dailyMealOrder == null) {
                return Result.error("大订单不存在");
            }

            // 验证权限:只能确认自己家庭的大订单
            if (!dailyMealOrder.getFamilyId().equals(wxUser.getFamilyId())) {
                return Result.error("无权限操作");
            }

            // 获取选中的菜品ID列表
            List<Long> dishIds = null;
            if (requestBody != null && requestBody.containsKey("dishIds")) {
                Object dishIdsObj = requestBody.get("dishIds");
                if (dishIdsObj instanceof List) {
                    dishIds = new ArrayList<>();
                    for (Object obj : (List<?>) dishIdsObj) {
                        if (obj instanceof Number) {
                            dishIds.add(((Number) obj).longValue());
                        }
                    }
                }
            }

            // 调用Service层方法,传递菜品ID列表
            dailyMealOrderService.confirmOrder(id, userId, dishIds);
            return Result.success("确认成功");
        } catch (Exception e) {
            log.error("确认发布大订单失败", e);
            return Result.error("确认失败: " + e.getMessage());
        }
    }

    /**
     * 查询历史大订单
     */
    @Operation(summary = "查询历史大订单", description = "查询历史大订单列表")
    @GetMapping("/history")
    public Result<List<DailyMealOrder>> getHistory(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = jwtUtil.getUserId(token);
            WxUser wxUser = wxUserService.getById(userId);
            if (wxUser == null || wxUser.getFamilyId() == null) {
                return Result.error("用户信息不完整");
            }

            LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;

            List<DailyMealOrder> orders = dailyMealOrderService.getHistoryOrders(
                    wxUser.getFamilyId(),
                    start,
                    end);

            return Result.success(orders);
        } catch (Exception e) {
            log.error("查询历史大订单失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}
