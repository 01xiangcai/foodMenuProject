package com.yao.food_menu.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话成员实体类
 */
@Data
@TableName("chat_conversation_member")
public class ChatConversationMember implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 用户ID
     */
    private Long wxUserId;

    /**
     * 用户在会话中的昵称（可选）
     */
    private String nickname;

    /**
     * 角色: 0-普通成员, 1-群主/管理员
     */
    private Integer role;

    /**
     * 最后已读消息ID
     */
    private Long lastReadMessageId;

    /**
     * 未读消息数
     */
    private Integer unreadCount;

    /**
     * 是否免打扰: 0-否, 1-是
     */
    private Integer muted;

    /**
     * 加入时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除: 0-未删除, 1-已删除
     */
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

    // ========== 非数据库字段 ==========

    /**
     * 用户信息（非数据库字段）
     */
    @TableField(exist = false)
    private WxUser wxUser;

    /**
     * 角色常量
     */
    public static final int ROLE_MEMBER = 0; // 普通成员
    public static final int ROLE_ADMIN = 1; // 群主/管理员
}
