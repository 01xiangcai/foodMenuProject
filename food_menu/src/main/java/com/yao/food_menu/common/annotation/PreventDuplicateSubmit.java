package com.yao.food_menu.common.annotation;

import java.lang.annotation.*;

/**
 * 防重复提交注解
 * 用于防止用户在短时间内重复提交相同的请求
 * 
 * 使用示例：
 * @PreventDuplicateSubmit(interval = 5000)
 * public Result<?> submitOrder() { ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PreventDuplicateSubmit {

    /**
     * 防重复提交的时间间隔（毫秒）
     * 默认：5秒
     */
    long interval() default 5000;

    /**
     * 提示信息
     * 默认：请勿重复提交
     */
    String message() default "请勿重复提交";

    /**
     * 是否基于用户维度
     * true: 同一用户不能重复提交
     * false: 同一IP不能重复提交
     * 默认：true（基于用户）
     */
    boolean byUser() default true;
}

