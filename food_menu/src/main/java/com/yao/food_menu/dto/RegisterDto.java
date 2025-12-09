package com.yao.food_menu.dto;

import lombok.Data;

/**
 * 用户注册DTO
 */
@Data
public class RegisterDto {
    // 用户名
    private String username;
    // 密码
    private String password;
    // 昵称
    private String nickname;
    // 手机号
    private String phone;
    // 家庭ID（超级管理员可以设置）
    private Long familyId;
}
