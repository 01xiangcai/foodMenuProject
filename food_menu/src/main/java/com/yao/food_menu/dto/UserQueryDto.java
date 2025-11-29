package com.yao.food_menu.dto;

import lombok.Data;

/**
 * 用户查询DTO,用于分页和过滤
 */
@Data
public class UserQueryDto {

    // 页码
    private Integer page = 1;

    // 每页大小
    private Integer pageSize = 10;

    // 用户名(模糊搜索)
    private String username;

    // 手机号(模糊搜索)
    private String phone;

    // 姓名(模糊搜索)
    private String name;

    // 用户类型: 0-管理员, 1-微信用户
    private Integer type;

    // 状态: 0-禁用, 1-启用
    private Integer status;

    // 家庭ID（超级管理员可以筛选特定家庭）
    private Long familyId;
}
