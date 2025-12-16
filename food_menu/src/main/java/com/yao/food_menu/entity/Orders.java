package com.yao.food_menu.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@TableName("orders")
public class Orders implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    // 订单号
    private String orderNumber;

    // 用户ID
    private Long userId;

    // 家庭ID
    private Long familyId;

    // 总金额
    private BigDecimal totalAmount;

    // 订单状态: 0-待接单, 1-准备中, 2-配送中, 3-已完成, 4-已取消, 5-待支付
    private Integer status;

    // 接单时间
    private LocalDateTime acceptTime;

    // 配送时间
    private LocalDateTime deliveryTime;

    // 完成时间
    private LocalDateTime completeTime;

    // 备注
    private String remark;

    // 支付方式: 1=余额支付, 2=模拟支付
    private Integer payMethod;

    // 支付状态: 0=未支付, 1=已支付, 2=已退款
    private Integer payStatus;

    // 支付时间
    private LocalDateTime payTime;

    // 餐次类型: BREAKFAST/LUNCH/DINNER
    private String mealPeriod;

    // 大订单ID
    private Long dailyMealOrderId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 逻辑删除: 0-未删除, 1-已删除
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

    // ========== 常量定义 ==========

    // 支付方式常量
    public static final int PAY_METHOD_WALLET = 1; // 余额支付
    public static final int PAY_METHOD_MOCK = 2; // 模拟支付

    // 支付状态常量
    public static final int PAY_STATUS_UNPAID = 0; // 未支付
    public static final int PAY_STATUS_PAID = 1; // 已支付
    public static final int PAY_STATUS_REFUNDED = 2; // 已退款

    // 订单状态常量
    public static final int STATUS_PENDING = 0; // 待接单
    public static final int STATUS_PREPARING = 1; // 准备中
    public static final int STATUS_DELIVERING = 2; // 配送中
    public static final int STATUS_COMPLETED = 3; // 已完成
    public static final int STATUS_CANCELLED = 4; // 已取消
    public static final int STATUS_UNPAID = 5; // 待支付
}
