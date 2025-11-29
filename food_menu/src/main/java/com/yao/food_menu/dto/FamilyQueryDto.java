package com.yao.food_menu.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 家庭查询参数对象
 */
@Data
public class FamilyQueryDto implements Serializable {

    private static final long serialVersionUID = 1L;

    // 页码
    private Integer page = 1;

    // 每页大小
    private Integer pageSize = 10;

    // 家庭名称(模糊查询)
    private String name;

    // 邀请码
    private String inviteCode;

    // 状态: 0-禁用, 1-正常
    private Integer status;
}
