package com.yao.food_menu.dto;

import lombok.Data;

/**
 * 用户创建/更新DTO
 */
@Data
public class UserDto {

    private Long id;

    // 用户名(创建时必填)
    private String username;

    // 密码(创建时必填,更新时可选)
    private String password;

    // 手机号
    private String phone;

    // 姓名
    private String name;

    // 头像URL
    private String avatar;

    // 用户类型: 0-管理员, 1-微信用户
    private Integer type;

    // 状态: 0-禁用, 1-启用
    private Integer status;

    // 家庭ID（超级管理员可以设置）
    private Long familyId;

    // 角色: 0-普通管理员, 1-家庭管理员, 2-超级管理员
    private Integer role;
}
