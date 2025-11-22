package com.yao.food_menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DTO for updating user information
 */
@Data
@Schema(description = "用户信息更新对象")
public class UpdateUserDto {

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "头像URL(OSS object key)", example = "avatars/2025-11-22/xxx.jpg")
    private String avatar;

    @Schema(description = "性别: 0-未知, 1-男, 2-女", example = "1")
    private Integer gender;
}
