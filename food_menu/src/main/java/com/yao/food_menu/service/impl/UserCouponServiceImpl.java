package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.common.context.FamilyContext;
import com.yao.food_menu.common.exception.BusinessException;
import com.yao.food_menu.dto.UserCouponDto;
import com.yao.food_menu.entity.UserCoupon;
import com.yao.food_menu.mapper.UserCouponMapper;
import com.yao.food_menu.service.UserCouponService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户优惠券服务实现类
 */
@Service
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon>
        implements UserCouponService {

    @Override
    public void issueCoupon(Long userId, String couponName, String couponType, BigDecimal couponValue,
            BigDecimal minOrderAmount, Integer validDays, String sourceType, Long sourceId) {
        UserCoupon coupon = new UserCoupon();
        coupon.setWxUserId(userId);
        coupon.setCouponName(couponName);
        coupon.setCouponType(couponType);
        coupon.setCouponValue(couponValue);
        coupon.setMinOrderAmount(minOrderAmount);
        coupon.setSourceType(sourceType);
        coupon.setSourceId(sourceId);
        coupon.setStatus(0); // 未使用
        coupon.setReceiveTime(LocalDateTime.now());
        coupon.setExpireTime(LocalDateTime.now().plusDays(validDays));
        coupon.setFamilyId(FamilyContext.getCurrentFamilyId());

        this.save(coupon);
    }

    @Override
    public List<UserCouponDto> getUserCoupons(Long userId, Integer status) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getWxUserId, userId)
                .eq(status != null, UserCoupon::getStatus, status)
                .orderByDesc(UserCoupon::getReceiveTime);

        List<UserCoupon> coupons = this.list(wrapper);

        return coupons.stream()
                .map(coupon -> {
                    UserCouponDto dto = new UserCouponDto();
                    BeanUtils.copyProperties(coupon, dto);

                    // 判断是否可用
                    boolean available = coupon.getStatus() == 0 &&
                            LocalDateTime.now().isBefore(coupon.getExpireTime());
                    dto.setAvailable(available);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void useCoupon(Long couponId, Long orderId) {
        UserCoupon coupon = this.getById(couponId);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }

        if (coupon.getStatus() != 0) {
            throw new BusinessException("优惠券已使用或已过期");
        }

        if (LocalDateTime.now().isAfter(coupon.getExpireTime())) {
            coupon.setStatus(2); // 已过期
            this.updateById(coupon);
            throw new BusinessException("优惠券已过期");
        }

        coupon.setStatus(1); // 已使用
        coupon.setUseTime(LocalDateTime.now());
        coupon.setOrderId(orderId);
        this.updateById(coupon);
    }

    @Override
    public List<UserCouponDto> getAvailableCoupons(Long userId, BigDecimal orderAmount) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getWxUserId, userId)
                .eq(UserCoupon::getStatus, 0) // 未使用
                .le(UserCoupon::getMinOrderAmount, orderAmount) // 满足最低消费
                .gt(UserCoupon::getExpireTime, LocalDateTime.now()) // 未过期
                .orderByDesc(UserCoupon::getCouponValue);

        List<UserCoupon> coupons = this.list(wrapper);

        return coupons.stream()
                .map(coupon -> {
                    UserCouponDto dto = new UserCouponDto();
                    BeanUtils.copyProperties(coupon, dto);
                    dto.setAvailable(true);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
