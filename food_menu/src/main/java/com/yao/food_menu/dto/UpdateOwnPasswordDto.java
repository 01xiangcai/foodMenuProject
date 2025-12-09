package com.yao.food_menu.dto;

import lombok.Data;

/**
 * 修改个人密码DTO
 */
@Data
public class UpdateOwnPasswordDto {
    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;
}
