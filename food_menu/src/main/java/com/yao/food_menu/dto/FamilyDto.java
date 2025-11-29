package com.yao.food_menu.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 家庭数据传输对象
 */
@Data
public class FamilyDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    // 家庭名称
    private String name;

    // 家庭描述
    private String description;

    // 邀请码
    private String inviteCode;

    // 状态: 0-禁用, 1-正常
    private Integer status;
}
