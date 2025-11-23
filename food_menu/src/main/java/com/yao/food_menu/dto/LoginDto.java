package com.yao.food_menu.dto;

import lombok.Data;

/**
 * 用户登录DTO
 */
@Data
public class LoginDto {

    // 登录类型: 1-用户名/密码, 2-手机号/验证码
    private Integer type;

    // 用户名(类型1使用)
    private String username;

    // 密码(类型1使用)
    private String password;

    // 手机号(类型2使用)
    private String phone;

    // 验证码(类型2使用)
    private String code;
}
