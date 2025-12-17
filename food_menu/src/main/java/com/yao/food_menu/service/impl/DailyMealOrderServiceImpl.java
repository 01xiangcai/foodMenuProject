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

        // 查询该大订单下的所有小订单
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getDailyMealOrderId, dailyMealOrderId);
        wrapper.ne(Orders::getStatus, Orders.STATUS_CANCELLED); // 排除已取消的订单
        wrapper.eq(Orders::getPayStatus, Orders.PAY_STATUS_PAID); // 只统计已支付的订单
        List<Orders> ordersList = ordersMapper.selectList(wrapper);

        // 统计总金额、参与人数、菜品数量
        BigDecimal totalAmount = BigDecimal.ZERO;
        Set<Long> userIds = new HashSet<>();
        int dishCount = 0;

        for (Orders order : ordersList) {
            totalAmount = totalAmount.add(order.getTotalAmount());
            userIds.add(order.getUserId());

            // 统计菜品数量(累加每个菜品的数量)
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getOrderId, order.getId());
            List<OrderItem> items = orderItemService.list(itemWrapper);
            for (OrderItem item : items) {
                dishCount += item.getQuantity(); // 累加数量
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

        // 如果提供了菜品ID列表,则将选中的菜品插入到发布记录表
        if (dishIds != null && !dishIds.isEmpty()) {
            // 获取该大订单下的所有小订单
            LambdaQueryWrapper<Orders> ordersWrapper = new LambdaQueryWrapper<>();
            ordersWrapper.eq(Orders::getDailyMealOrderId, dailyMealOrderId);
            List<Orders> ordersList = ordersService.list(ordersWrapper);

            // 准备发布记录列表
            List<com.yao.food_menu.entity.DailyMealPublishItem> publishItems = new ArrayList<>();

            // 统计数据
            BigDecimal totalAmount = BigDecimal.ZERO;
            int totalDishCount = 0;
            Set<Long> userIds = new HashSet<>();

            // 遍历每个小订单,查找选中的菜品
            for (Orders order : ordersList) {
                // 获取该订单的所有菜品项
                LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
                itemWrapper.eq(OrderItem::getOrderId, order.getId());
                List<OrderItem> allItems = orderItemService.list(itemWrapper);

                boolean hasPublishedItem = false; // 标记该订单是否有被发布的菜品

                // 筛选出被选中的菜品项
                for (OrderItem item : allItems) {
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

                        // 计算小计
                        BigDecimal subtotal = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
                        publishItem.setSubtotal(subtotal);

                        publishItem.setUserId(order.getUserId());

                        // 查询用户昵称
                        com.yao.food_menu.entity.WxUser wxUser = wxUserService.getById(order.getUserId());
                        if (wxUser != null) {
                            publishItem.setUserNickname(wxUser.getNickname());
                        }

                        publishItems.add(publishItem);

                        // 累加统计数据
                        totalAmount = totalAmount.add(subtotal);
                        totalDishCount += item.getQuantity();
                        userIds.add(order.getUserId());
                    }
                }

                // 如果该订单有被发布的菜品,更新订单状态为准备中
                if (hasPublishedItem) {
                    order.setStatus(Orders.STATUS_PREPARING);
                    ordersService.updateById(order);
                }
            }

            // 批量插入发布记录
            if (!publishItems.isEmpty()) {
                dailyMealPublishItemService.batchInsert(publishItems);
            }

            // 更新大订单的统计数据(基于发布的菜品)
            dailyMealOrder.setTotalAmount(totalAmount);
            dailyMealOrder.setDishCount(totalDishCount);
            dailyMealOrder.setMemberCount(userIds.size());
        }

        // 更新大订单状态为已确认
        dailyMealOrder.setStatus(DailyMealOrder.STATUS_CONFIRMED);
        dailyMealOrder.setConfirmedBy(confirmedBy);
        dailyMealOrder.setConfirmedTime(LocalDateTime.now());

        updateById(dailyMealOrder);
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
