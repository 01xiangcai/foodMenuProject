package com.yao.food_menu.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户钱包实体类
 */
@Data
@TableName("wx_user_wallet")
public class UserWallet implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联微信用户ID
     */
    private String wxUserId;

    /**
     * 可用余额
     */
    private BigDecimal balance;

    /**
     * 冻结金额
     */
    private BigDecimal frozenAmount;

    /**
     * 支付密码(BCrypt加密),返回时忽略
     */
    @JsonIgnore
    private String payPassword;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除: 0-未删除, 1-已删除
     */
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

    /**
     * 非数据库字段: 用户昵称(查询时关联)
     */
    @TableField(exist = false)
    private String nickname;

    /**
     * 非数据库字段: 用户手机号(查询时关联)
     */
    @TableField(exist = false)
    private String phone;
}
