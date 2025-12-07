package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.common.context.FamilyContext;
import com.yao.food_menu.dto.OrdersDto;
import com.yao.food_menu.dto.PayDto;
import com.yao.food_menu.entity.Dish;
import com.yao.food_menu.entity.OrderItem;
import com.yao.food_menu.entity.Orders;
import com.yao.food_menu.entity.UserWallet;
import com.yao.food_menu.mapper.OrdersMapper;
import com.yao.food_menu.service.DishService;
import com.yao.food_menu.service.OrderItemService;
import com.yao.food_menu.service.OrdersService;
import com.yao.food_menu.service.WalletService;
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

    @Autowired
    private WalletService walletService;

    private static final AtomicLong orderCounter = new AtomicLong(1);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(OrdersDto ordersDto) {
        log.info("Received order DTO: {}", ordersDto);

        // 校验订单明细
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

        // 生成订单号
        String orderNumber = generateOrderNumber();
        ordersDto.setOrderNumber(orderNumber);
        ordersDto.setStatus(Orders.STATUS_UNPAID); // 待支付 (5)

        // 计算总金额并填充菜品信息
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            Dish dish = dishService.getById(item.getDishId());
            if (dish != null) {
                item.setDishName(dish.getName());
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

        // 设置初始支付状态为未支付
        ordersDto.setPayStatus(Orders.PAY_STATUS_UNPAID);
        // 初始支付方式为空，等支付时再设置
        ordersDto.setPayMethod(null);

        // 保存订单
        this.save(ordersDto);

        // 保存订单明细
        Long orderId = ordersDto.getId();
        Long familyId = ordersDto.getFamilyId();
        for (OrderItem item : orderItems) {
            item.setOrderId(orderId);
            item.setFamilyId(familyId);
        }
        orderItemService.saveBatch(orderItems);

        log.info("Order created (pending payment): {}", orderNumber);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(PayDto payDto) {
        String orderNumber = payDto.getOrderNo();
        log.info("Process payment for order: {}", orderNumber);

        // 查询订单
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Orders> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getOrderNumber, orderNumber);
        Orders order = this.getOne(queryWrapper);

        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getPayStatus() == Orders.PAY_STATUS_PAID) {
            throw new RuntimeException("订单已支付");
        }

        if (order.getStatus() == Orders.STATUS_CANCELLED) {
            throw new RuntimeException("订单已取消，无法支付");
        }

        // 验证用户
        // 注意：这里可能需要更多的安全校验，比如验证调用者是否为订单拥有者
        // 但由于是在 internal service 中，我们假设 Controller 层已经做了一些基本的校验
        // 或者我们在这里再校验一次（需要传入 userId）

        Integer payMethod = payDto.getPayMethod();
        if (payMethod == null) {
            payMethod = Orders.PAY_METHOD_MOCK;
        }

        BigDecimal amount = order.getTotalAmount();

        if (payMethod == Orders.PAY_METHOD_WALLET) {
            // 余额支付
            String wxUserId = order.getUserId().toString();

            // 校验钱包余额
            UserWallet wallet = walletService.getWalletByUserId(wxUserId);
            if (wallet == null) {
                throw new RuntimeException("钱包不存在，请联系管理员");
            }

            BigDecimal availableBalance = wallet.getBalance().subtract(wallet.getFrozenAmount());
            if (availableBalance.compareTo(amount) < 0) {
                throw new RuntimeException("余额不足，请充值或者联系管理员");
            }

            // 设置支付金额
            payDto.setAmount(amount);
            payDto.setRemark("订单消费: " + orderNumber);

            boolean paySuccess = walletService.pay(wxUserId, payDto);
            if (!paySuccess) {
                throw new RuntimeException("支付失败，请重试");
            }
        }

        // 更新订单支付状态
        order.setPayMethod(payMethod);
        order.setPayStatus(Orders.PAY_STATUS_PAID);
        order.setPayTime(LocalDateTime.now());

        // 支付成功后，状态流转为待接单
        order.setStatus(Orders.STATUS_PENDING);

        this.updateById(order);

        log.info("Order paid successfully: {}", orderNumber);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Orders orders = this.getById(id);
        if (orders == null) {
            throw new RuntimeException("订单不存在");
        }

        Integer oldStatus = orders.getStatus();
        orders.setStatus(status);

        // 根据状态记录对应的时间
        LocalDateTime now = LocalDateTime.now();

        // 校验：如果是接单操作(0 -> 1)，必须已支付
        // 校验：如果是接单操作(0 -> 1)，必须已支付
        if (status == Orders.STATUS_PREPARING) {
            if (oldStatus == Orders.STATUS_UNPAID) {
                throw new RuntimeException("订单未支付，无法接单");
            }
            if (oldStatus == Orders.STATUS_PENDING) {
                if (orders.getPayStatus() != Orders.PAY_STATUS_PAID) {
                    throw new RuntimeException("订单未支付，无法接单");
                }
                // 接单
                orders.setAcceptTime(now);
            }
        } else if (status == Orders.STATUS_DELIVERING && oldStatus != Orders.STATUS_DELIVERING) {
        } else if (status == Orders.STATUS_DELIVERING && oldStatus != Orders.STATUS_DELIVERING) {
            // 开始配送
            orders.setDeliveryTime(now);
        } else if (status == Orders.STATUS_COMPLETED && oldStatus != Orders.STATUS_COMPLETED) {
            // 订单完成
            orders.setCompleteTime(now);
        } else if (status == Orders.STATUS_CANCELLED && oldStatus != Orders.STATUS_CANCELLED) {
            // 订单取消 - 处理退款
            handleOrderCancelRefund(orders);
        }

        this.updateById(orders);

        log.info("Order status updated: {} -> {}", id, status);

        // 如果订单状态变更为已完成(2或3),更新菜品统计
        if ((status == Orders.STATUS_DELIVERING || status == Orders.STATUS_COMPLETED) && !status.equals(oldStatus)) {
            log.info("Order completed, updating dish statistics for order: {}", id);
            try {
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItem> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                queryWrapper.eq(OrderItem::getOrderId, id);
                List<OrderItem> orderItems = orderItemService.list(queryWrapper);

                List<Long> dishIds = orderItems.stream()
                        .map(OrderItem::getDishId)
                        .distinct()
                        .collect(java.util.stream.Collectors.toList());

                dishStatisticsService.batchIncrementOrderCount(dishIds);
                log.info("Dish statistics updated for {} dishes", dishIds.size());
            } catch (Exception e) {
                log.error("Failed to update dish statistics for order: {}", id, e);
            }
        }
    }

    /**
     * 处理订单取消退款
     */
    private void handleOrderCancelRefund(Orders orders) {
        // 只有余额支付且已支付的订单才需要退款
        if (orders.getPayMethod() != null
                && orders.getPayMethod() == Orders.PAY_METHOD_WALLET
                && orders.getPayStatus() != null
                && orders.getPayStatus() == Orders.PAY_STATUS_PAID) {

            try {
                String wxUserId = orders.getUserId().toString();
                BigDecimal refundAmount = orders.getTotalAmount();

                // 调用钱包服务退款
                walletService.refund(wxUserId, refundAmount, orders.getOrderNumber());

                // 更新订单支付状态为已退款
                orders.setPayStatus(Orders.PAY_STATUS_REFUNDED);

                log.info("订单 {} 退款成功，金额: {}", orders.getOrderNumber(), refundAmount);
            } catch (Exception e) {
                log.error("订单 {} 退款失败: {}", orders.getOrderNumber(), e.getMessage());
                throw new RuntimeException("退款失败: " + e.getMessage());
            }
        }
    }

    /**
     * 生成订单号
     * 格式: FM + yyyyMMddHHmmss + 4位序号
     */
    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long sequence = orderCounter.getAndIncrement() % 10000;
        return "FM" + timestamp + String.format("%04d", sequence);
    }
}
