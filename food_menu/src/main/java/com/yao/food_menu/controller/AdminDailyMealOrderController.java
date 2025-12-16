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
import com.yao.food_menu.mapper.OrdersMapper;
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
 * Admin端大订单Controller
 */
@Tag(name = "Admin大订单统计", description = "Admin端大订单统计查询")
@RestController
@RequestMapping("/admin/daily-meal-order")
@Slf4j
public class AdminDailyMealOrderController {

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

                // 查询用户信息
                WxUser member = wxUserService.getById(order.getUserId());
                if (member != null) {
                    memberOrder.put("userId", member.getId());
                    memberOrder.put("nickname", member.getNickname());
                    memberOrder.put("avatar", member.getAvatar());
                }

                // 查询订单项
                LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
                itemWrapper.eq(OrderItem::getOrderId, order.getId());
                List<OrderItem> items = orderItemService.list(itemWrapper);
                memberOrder.put("items", items);

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
}
