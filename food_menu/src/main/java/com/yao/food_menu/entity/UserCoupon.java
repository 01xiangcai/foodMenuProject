package com.yao.food_menu.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券实体类
 */
@Data
public class UserCoupon implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    // 用户ID
    private Long wxUserId;

    // 优惠券名称
    private String couponName;

    // 优惠券类型: DISCOUNT(折扣券), CASH(代金券), FREE_DISH(免费菜品券)
    private String couponType;

    // 优惠券面值
    private BigDecimal couponValue;

    // 最低消费金额
    private BigDecimal minOrderAmount;

    // 来源类型: ACTIVITY(活动), MANUAL(手动发放), SYSTEM(系统赠送)
    private String sourceType;

    // 来源ID(如活动ID)
    private Long sourceId;

    // 状态: 0-未使用, 1-已使用, 2-已过期
    private Integer status;

    // 领取时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime receiveTime;

    // 使用时间
    private LocalDateTime useTime;

    // 过期时间
    private LocalDateTime expireTime;

    // 使用的订单ID
    private Long orderId;

    // 家庭ID
    private Long familyId;

    // 逻辑删除: 0-未删除, 1-已删除
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
