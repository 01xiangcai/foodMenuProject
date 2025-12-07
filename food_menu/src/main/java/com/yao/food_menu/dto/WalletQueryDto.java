package com.yao.food_menu.dto;

import lombok.Data;

/**
 * 钱包列表查询参数
 */
@Data
public class WalletQueryDto {

    /**
     * 当前页码
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;

    /**
     * 微信用户ID (模糊搜索)
     */
    private String wxUserId;

    /**
     * 用户昵称 (模糊搜索)
     */
    private String nickname;

    /**
     * 家庭ID (权限过滤)
     */
    private Long familyId;
}
