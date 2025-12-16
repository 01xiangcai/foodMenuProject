package com.yao.food_menu.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yao.food_menu.entity.OrderItem;
import com.yao.food_menu.entity.Orders;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * 订单DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrdersDto extends Orders {

    // 订单项列表
    @JsonProperty("orderItems")
    private List<OrderItem> orderItems;

    // 用户信息(用于管理面板)
    private String userNickname; // 用户昵称
    private String userPhone; // 用户手机号
    private String userAvatar; // 用户头像

    // 支付密码（余额支付时使用，不持久化）
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String payPassword;

    // 餐次名称(用于前端显示)
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String mealPeriodName;
}
