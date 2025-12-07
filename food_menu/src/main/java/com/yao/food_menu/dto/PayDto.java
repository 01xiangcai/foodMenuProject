package com.yao.food_menu.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 余额支付请求参数
 */
@Data
public class PayDto {

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付密码
     */
    private String payPassword;

    /**
     * 关联订单号
     */
    private String orderNo;

    /**
     * 备注说明
     */
    private String remark;
}
