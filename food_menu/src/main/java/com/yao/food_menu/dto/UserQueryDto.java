package com.yao.food_menu.dto;

import lombok.Data;

/**
 * User query DTO for pagination and filtering
 */
@Data
public class UserQueryDto {

    // Page number
    private Integer page = 1;

    // Page size
    private Integer pageSize = 10;

    // Username (fuzzy search)
    private String username;

    // Phone number (fuzzy search)
    private String phone;

    // Name (fuzzy search)
    private String name;

    // User type: 0-admin, 1-wx user
    private Integer type;

    // Status: 0-disabled, 1-enabled
    private Integer status;
}
