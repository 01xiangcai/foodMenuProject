package com.yao.food_menu.dto;

import lombok.Data;

/**
 * 更新个人信息DTO
 */
@Data
public class UpdateProfileDto {
    /**
     * 姓名
     */
    private String name;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像
     */
    private String avatar;
}
