package com.yao.food_menu.dto;

import lombok.Data;

/**
 * User login DTO
 */
@Data
public class LoginDto {

    // Login type: 1-username/password, 2-phone/code
    private Integer type;

    // Username (for type 1)
    private String username;

    // Password (for type 1)
    private String password;

    // Phone number (for type 2)
    private String phone;

    // Verification code (for type 2)
    private String code;
}
