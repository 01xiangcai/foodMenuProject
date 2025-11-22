package com.yao.food_menu.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * WeChat Mini Program User entity
 */
@Data
@TableName("wx_user")
public class WxUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    // WeChat OpenID (unique identifier from WeChat)
    private String openid;

    // WeChat UnionID (for multi-app scenarios, reserved)
    private String unionid;

    // Username (for username/password login)
    private String username;

    // Password (encrypted, for username/password login)
    private String password;

    // Phone number
    private String phone;

    // Nickname
    private String nickname;

    // Avatar URL
    private String avatar;

    // Gender: 0-unknown, 1-male, 2-female
    private Integer gender;

    // Status: 0-disabled, 1-normal
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
