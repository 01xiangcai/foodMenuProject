package com.yao.food_menu.common.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流注解
 * 使用Guava RateLimiter实现单机限流
 * 
 * 使用示例：
 * @RateLimiter(qps = 10, timeout = 500)
 * public Result<?> someMethod() { ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {

    /**
     * 限流QPS（每秒请求数）
     * 默认：10
     */
    double qps() default 10.0;

    /**
     * 获取令牌超时时间（毫秒）
     * 超时则拒绝请求
     * 默认：500ms
     */
    long timeout() default 500;

    /**
     * 超时时间单位
     * 默认：毫秒
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /**
     * 限流提示信息
     * 默认：请求过于频繁，请稍后再试
     */
    String message() default "请求过于频繁，请稍后再试";

    /**
     * 限流范围
     * IP: 按IP限流
     * USER: 按用户ID限流
     * GLOBAL: 全局限流
     */
    LimitType limitType() default LimitType.IP;

    /**
     * 限流范围枚举
     */
    enum LimitType {
        /**
         * 按IP限流
         */
        IP,
        
        /**
         * 按用户ID限流
         */
        USER,
        
        /**
         * 全局限流（所有请求共享）
         */
        GLOBAL
    }
}

