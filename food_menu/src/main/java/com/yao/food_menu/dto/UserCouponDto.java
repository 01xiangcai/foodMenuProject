package com.yao.food_menu.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券DTO
 */
@Data
public class UserCouponDto {

    private Long id;

    // 优惠券名称
    private String couponName;

    // 优惠券类型
    private String couponType;

    // 优惠券面值
    private BigDecimal couponValue;

    // 最低消费金额
    private BigDecimal minOrderAmount;

    // 来源类型
    private String sourceType;

    // 状态
    private Integer status;

    // 领取时间
    private LocalDateTime receiveTime;

    // 过期时间
    private LocalDateTime expireTime;

    // 是否可用
    private Boolean available;
}
