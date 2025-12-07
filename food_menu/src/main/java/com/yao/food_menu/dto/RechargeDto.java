package com.yao.food_menu.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 充值请求参数
 */
@Data
public class RechargeDto {

    /**
     * 微信用户ID
     */
    private String wxUserId;

    /**
     * 充值金额 (必须大于0)
     */
    private BigDecimal amount;

    /**
     * 备注说明
     */
    private String remark;
}
