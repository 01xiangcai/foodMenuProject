package com.yao.food_menu.dto;

import lombok.Data;

/**
 * User creation/update DTO
 */
@Data
public class UserDto {

    private Long id;

    // Username (required for creation)
    private String username;

    // Password (required for creation, optional for update)
    private String password;

    // Phone number
    private String phone;

    // Name
    private String name;

    // Avatar URL
    private String avatar;

    // User type: 0-admin, 1-wx user
    private Integer type;

    // Status: 0-disabled, 1-enabled
    private Integer status;
}
