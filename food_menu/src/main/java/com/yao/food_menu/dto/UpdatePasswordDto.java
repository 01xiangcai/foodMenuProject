package com.yao.food_menu.dto;

import lombok.Data;

/**
 * 修改密码DTO(管理员操作)
 */
@Data
public class UpdatePasswordDto {

    // 要修改密码的用户ID
    private Long userId;

    // 新密码(明文,后端会加密存储)
    private String newPassword;
}
