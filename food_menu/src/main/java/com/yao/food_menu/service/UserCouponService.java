package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.dto.UserCouponDto;
import com.yao.food_menu.entity.UserCoupon;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户优惠券服务接口
 */
public interface UserCouponService extends IService<UserCoupon> {

    /**
     * 发放优惠券
     */
    void issueCoupon(Long userId, String couponName, String couponType, BigDecimal couponValue,
            BigDecimal minOrderAmount, Integer validDays, String sourceType, Long sourceId);

    /**
     * 获取用户优惠券列表
     */
    List<UserCouponDto> getUserCoupons(Long userId, Integer status);

    /**
     * 使用优惠券
     */
    void useCoupon(Long couponId, Long orderId);

    /**
     * 获取订单可用优惠券
     */
    List<UserCouponDto> getAvailableCoupons(Long userId, BigDecimal orderAmount);
}
