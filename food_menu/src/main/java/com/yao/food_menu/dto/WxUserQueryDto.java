package com.yao.food_menu.dto;

import lombok.Data;

/**
 * WxUser query DTO for pagination and filtering
 */
@Data
public class WxUserQueryDto {

    // Page number
    private Integer page = 1;

    // Page size
    private Integer pageSize = 10;

    // Nickname (fuzzy search)
    private String nickname;

    // Phone number (fuzzy search)
    private String phone;

    // Username (fuzzy search)
    private String username;

    // Status: 0-disabled, 1-enabled
    private Integer status;
}
