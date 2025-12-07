package com.yao.food_menu.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包流水实体类
 */
@Data
@TableName("wx_wallet_transaction")
public class WalletTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联微信用户ID
     */
    private String wxUserId;

    /**
     * 交易类型: 1=后台充值, 2=订单消费, 3=退款冻结，4=取消退款
     */
    private Integer transType;

    /**
     * 变动金额
     */
    private BigDecimal amount;

    /**
     * 交易后余额
     */
    private BigDecimal balanceAfter;

    /**
     * 关联业务单号
     */
    private String relatedOrderNo;

    /**
     * 备注说明
     */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 非数据库字段: 用户昵称(查询时关联)
     */
    @TableField(exist = false)
    private String nickname;

    /**
     * 交易类型常量
     */
    public static final int TRANS_TYPE_RECHARGE = 1; // 后台充值
    public static final int TRANS_TYPE_CONSUME = 2; // 订单消费
    public static final int TRANS_TYPE_FREEZE = 3; // 退款冻结
    public static final int TRANS_TYPE_REFUND = 4; // 订单取消退款
}
