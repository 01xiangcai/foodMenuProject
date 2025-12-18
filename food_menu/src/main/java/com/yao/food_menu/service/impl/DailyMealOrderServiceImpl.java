package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.entity.DailyMealOrder;
import com.yao.food_menu.entity.OrderItem;
import com.yao.food_menu.entity.Orders;
import com.yao.food_menu.mapper.DailyMealOrderMapper;
import com.yao.food_menu.mapper.OrdersMapper;
import com.yao.food_menu.service.DailyMealOrderService;
import com.yao.food_menu.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 大订单服务实现
 */
@Service
@Slf4j
public class DailyMealOrderServiceImpl extends ServiceImpl<DailyMealOrderMapper, DailyMealOrder>
        implements DailyMealOrderService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private com.yao.food_menu.service.OrdersService ordersService;

    @Autowired
    private com.yao.food_menu.service.DailyMealPublishItemService dailyMealPublishItemService;

    @Autowired
    private com.yao.food_menu.service.WxUserService wxUserService;

    @Autowired
    private com.yao.food_menu.service.WalletService walletService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DailyMealOrder createOrGet(Long familyId, LocalDate orderDate, String mealPeriod) {
        // 查询是否已存在
        LambdaQueryWrapper<DailyMealOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DailyMealOrder::getFamilyId, familyId);
        wrapper.eq(DailyMealOrder::getOrderDate, orderDate);
        wrapper.eq(DailyMealOrder::getMealPeriod, mealPeriod);

        DailyMealOrder existing = getOne(wrapper);
        if (existing != null) {
            return existing;
        }

        // 创建新的大订单
        DailyMealOrder dailyMealOrder = new DailyMealOrder();
        dailyMealOrder.setFamilyId(familyId);
        dailyMealOrder.setOrderDate(orderDate);
        dailyMealOrder.setMealPeriod(mealPeriod);
        dailyMealOrder.setStatus(DailyMealOrder.STATUS_COLLECTING);
        dailyMealOrder.setTotalAmount(BigDecimal.ZERO);
        dailyMealOrder.setMemberCount(0);
        dailyMealOrder.setDishCount(0);

        save(dailyMealOrder);
        return dailyMealOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatistics(Long dailyMealOrderId) {
        DailyMealOrder dailyMealOrder = getById(dailyMealOrderId);
        if (dailyMealOrder == null) {
            return;
        }

        // 查询该大订单下的所有小订单: 包含正常订单 或者 已接受的迟到订单
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getDailyMealOrderId, dailyMealOrderId);
        wrapper.ne(Orders::getStatus, Orders.STATUS_CANCELLED); // 排除已取消的订单
        wrapper.eq(Orders::getPayStatus, Orders.PAY_STATUS_PAID); // 只统计已支付的订单

        // 修改点: 包含正常订单 OR 迟到但已接受的订单
        wrapper.and(w -> w.eq(Orders::getIsLateOrder, Orders.LATE_ORDER_NO)
                .or(ow -> ow.eq(Orders::getIsLateOrder, Orders.LATE_ORDER_YES)
                        .eq(Orders::getLateOrderStatus, Orders.LATE_STATUS_ACCEPTED)));

        List<Orders> ordersList = ordersMapper.selectList(wrapper);

        // 统计总金额、参与人数、菜品数量
        BigDecimal totalAmount = BigDecimal.ZERO;
        Set<Long> userIds = new HashSet<>();
        int dishCount = 0;

        boolean isConfirmed = dailyMealOrder.getStatus() == DailyMealOrder.STATUS_CONFIRMED;

        for (Orders order : ordersList) {
            BigDecimal orderEffectiveAmount = BigDecimal.ZERO;
            int orderEffectiveDishCount = 0;
            boolean hasEffectiveItem = false;

            // 查询订单项
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getOrderId, order.getId());
            List<OrderItem> items = orderItemService.list(itemWrapper);

            for (OrderItem item : items) {
                // 如果大订单已确认,则只统计[非迟到订单中的已发布项] 以及 [已接受的迟到订单中的所有项]
                // 注意: 迟到订单一旦被接受,其下所有项都应计入统计(因为它们没有发布筛选流程,或是整体接受)
                boolean shouldCount = true;
                if (isConfirmed && (order.getIsLateOrder() == null || order.getIsLateOrder() == Orders.LATE_ORDER_NO)) {
                    // 对于正常订单,只统计已发布的项
                    shouldCount = (item.getIsPublished() != null && item.getIsPublished() == 1);
                }

                if (shouldCount) {
                    orderEffectiveDishCount += item.getQuantity();
                    orderEffectiveAmount = orderEffectiveAmount
                            .add(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
                    hasEffectiveItem = true;
                }
            }

            if (hasEffectiveItem) {
                totalAmount = totalAmount.add(orderEffectiveAmount);
                dishCount += orderEffectiveDishCount;
                userIds.add(order.getUserId());
            }
        }

        // 更新大订单统计信息
        dailyMealOrder.setTotalAmount(totalAmount);
        dailyMealOrder.setMemberCount(userIds.size());
        dailyMealOrder.setDishCount(dishCount);

        updateById(dailyMealOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(Long dailyMealOrderId, Long confirmedBy, List<Long> dishIds) {
        DailyMealOrder dailyMealOrder = getById(dailyMealOrderId);
        if (dailyMealOrder == null) {
            throw new RuntimeException("大订单不存在");
        }

        if (dailyMealOrder.getStatus() == DailyMealOrder.STATUS_CONFIRMED) {
            throw new RuntimeException("大订单已确认,无法重复确认");
        }

        // 如果提供了菜品ID列表,则将选中的菜品标记为已发布
        if (dishIds != null && !dishIds.isEmpty()) {
            // 获取该大订单下的所有小订单
            LambdaQueryWrapper<Orders> ordersWrapper = new LambdaQueryWrapper<>();
            ordersWrapper.eq(Orders::getDailyMealOrderId, dailyMealOrderId);
            // 只处理正常订单(非迟到订单)
            ordersWrapper.eq(Orders::getIsLateOrder, Orders.LATE_ORDER_NO);
            List<Orders> ordersList = ordersMapper.selectList(ordersWrapper);

            // 准备发布记录列表
            List<com.yao.food_menu.entity.DailyMealPublishItem> publishItems = new ArrayList<>();

            // 遍历每个小订单,查找选中的菜品
            for (Orders order : ordersList) {
                // 只处理已支付的订单
                if (order.getPayStatus() == null || order.getPayStatus() != Orders.PAY_STATUS_PAID) {
                    continue;
                }

                // 获取该订单的所有菜品项
                LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
                itemWrapper.eq(OrderItem::getOrderId, order.getId());
                List<OrderItem> allItems = orderItemService.list(itemWrapper);

                boolean hasPublishedItem = false; // 标记该订单是否有被发布的菜品
                BigDecimal refundAmount = BigDecimal.ZERO; // 该订单需要退款的金额

                // 筛选出被选中的菜品项
                for (OrderItem item : allItems) {
                    BigDecimal itemSubtotal = item.getPrice().multiply(new BigDecimal(item.getQuantity()));

                    if (dishIds.contains(item.getId())) {
                        // 更新菜品的发布状态
                        item.setIsPublished(1);
                        orderItemService.updateById(item);
                        hasPublishedItem = true;

                        // 创建发布记录
                        com.yao.food_menu.entity.DailyMealPublishItem publishItem = new com.yao.food_menu.entity.DailyMealPublishItem();
                        publishItem.setDailyMealOrderId(dailyMealOrderId);
                        publishItem.setOrderItemId(item.getId());
                        publishItem.setOrderId(order.getId());
                        publishItem.setDishId(item.getDishId());
                        publishItem.setDishName(item.getDishName());
                        publishItem.setDishImage(item.getDishImage());
                        publishItem.setQuantity(item.getQuantity());
                        publishItem.setPrice(item.getPrice());
                        publishItem.setSubtotal(itemSubtotal);

                        publishItem.setUserId(order.getUserId());

                        // 查询用户昵称
                        com.yao.food_menu.entity.WxUser wxUser = wxUserService.getById(order.getUserId());
                        if (wxUser != null) {
                            publishItem.setUserNickname(wxUser.getNickname());
                        }

                        publishItems.add(publishItem);
                    } else {
                        // 未被选中的菜品,累计退款金额
                        refundAmount = refundAmount.add(itemSubtotal);
                    }
                }

                // 处理退款和订单状态
                if (hasPublishedItem) {
                    // 有部分菜品被发布,更新订单状态为准备中
                    order.setStatus(Orders.STATUS_PREPARING);
                    ordersMapper.updateById(order);

                    // 如果有需要退款的金额(部分退款)
                    if (refundAmount.compareTo(BigDecimal.ZERO) > 0 &&
                            order.getPayMethod() != null &&
                            order.getPayMethod() == Orders.PAY_METHOD_WALLET) {
                        try {
                            walletService.refund(
                                    order.getUserId().toString(),
                                    refundAmount,
                                    order.getOrderNumber() + "-部分退款");
                        } catch (Exception e) {
                            // 退款失败不影响发布流程,记录日志
                            log.error("订单 {} 部分退款失败: {}", order.getOrderNumber(), e.getMessage());
                        }
                    }
                } else {
                    // 所有菜品都未被采纳,整单取消并全额退款
                    order.setStatus(Orders.STATUS_CANCELLED);
                    ordersMapper.updateById(order);

                    // 全额退款
                    if (order.getPayMethod() != null &&
                            order.getPayMethod() == Orders.PAY_METHOD_WALLET) {
                        try {
                            walletService.refund(
                                    order.getUserId().toString(),
                                    order.getTotalAmount(),
                                    order.getOrderNumber() + "-整单取消退款");
                            order.setPayStatus(Orders.PAY_STATUS_REFUNDED);
                            ordersMapper.updateById(order);
                        } catch (Exception e) {
                            log.error("订单 {} 全额退款失败: {}", order.getOrderNumber(), e.getMessage());
                        }
                    }
                }
            }

            // 批量插入发布记录
            if (!publishItems.isEmpty()) {
                dailyMealPublishItemService.batchInsert(publishItems);
            }
        }

        // 更新大订单状态为已确认
        dailyMealOrder.setStatus(DailyMealOrder.STATUS_CONFIRMED);
        dailyMealOrder.setConfirmedBy(confirmedBy);
        dailyMealOrder.setConfirmedTime(LocalDateTime.now());

        updateById(dailyMealOrder);

        // 最后统一更新一次统计数据
        updateStatistics(dailyMealOrderId);
    }

    @Override
    public List<DailyMealOrder> getByFamilyAndDate(Long familyId, LocalDate orderDate) {
        return baseMapper.selectByFamilyAndDate(familyId, orderDate);
    }

    @Override
    public DailyMealOrder getDetailById(Long dailyMealOrderId) {
        // 这里可以扩展,关联查询所有小订单
        return getById(dailyMealOrderId);
    }

    @Override
    public List<DailyMealOrder> getHistoryOrders(Long familyId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<DailyMealOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DailyMealOrder::getFamilyId, familyId);
        wrapper.ge(startDate != null, DailyMealOrder::getOrderDate, startDate);
        wrapper.le(endDate != null, DailyMealOrder::getOrderDate, endDate);
        wrapper.orderByDesc(DailyMealOrder::getOrderDate);

        return list(wrapper);
    }

    @Override
    public boolean canModifyOrder(Long dailyMealOrderId) {
        DailyMealOrder dailyMealOrder = getById(dailyMealOrderId);
        if (dailyMealOrder == null) {
            return false;
        }

        // 已确认或已截止的订单不可修改
        return dailyMealOrder.getStatus() == DailyMealOrder.STATUS_COLLECTING;
    }
}
