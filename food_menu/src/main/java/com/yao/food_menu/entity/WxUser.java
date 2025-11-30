package com.yao.food_menu.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 微信小程序用户实体类
 */
@Data
@TableName("wx_user")
public class WxUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    // 微信OpenID(微信唯一标识符)
    private String openid;

    // 微信UnionID(多应用场景使用,预留)
    private String unionid;

    // 用户名(用于用户名/密码登录)
    private String username;

    // 密码(加密存储,用于用户名/密码登录)
    private String password;

    // 手机号
    private String phone;

    // 昵称
    private String nickname;

    // 头像URL
    private String avatar;

    // 本地头像路径
    private String localAvatar;

    // 性别: 0-未知, 1-男, 2-女
    private Integer gender;

    // 家庭ID
    private Long familyId;

    // 角色: 0-普通用户, 1-管理员
    private Integer role;

    // 状态: 0-禁用, 1-正常
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 头像更新次数(每日限制)
    private Integer avatarUpdateCount;

    // 头像最后更新日期
    private java.time.LocalDate avatarLastUpdateDate;

    // 逻辑删除: 0-未删除, 1-已删除
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}