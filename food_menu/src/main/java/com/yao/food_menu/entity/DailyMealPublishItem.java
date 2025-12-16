package com.yao.food_menu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 餐次发布菜品记录实体
 */
@Data
@TableName("daily_meal_publish_item")
public class DailyMealPublishItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 大订单ID
     */
    private Long dailyMealOrderId;

    /**
     * 订单项ID
     */
    private Long orderItemId;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 菜品ID
     */
    private Long dishId;

    /**
     * 菜品名称
     */
    private String dishName;

    /**
     * 菜品图片
     */
    private String dishImage;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 小计
     */
    private BigDecimal subtotal;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String userNickname;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
