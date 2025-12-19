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
 * 聊天会话实体类
 */
@Data
@TableName("chat_conversation")
public class ChatConversation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话类型: 1-私聊, 2-家庭群聊
     */
    private Integer type;

    /**
     * 所属家庭ID（群聊时必填）
     */
    private Long familyId;

    /**
     * 会话名称（群聊名，私聊时为空）
     */
    private String name;

    /**
     * 会话头像（群聊头像）
     */
    private String avatar;

    /**
     * 最后一条消息ID
     */
    private Long lastMessageId;

    /**
     * 最后消息时间（用于排序）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMessageTime;

    /**
     * 最后消息内容预览
     */
    private String lastMessageContent;

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
     * 未读消息数（非数据库字段，查询时计算）
     */
    @TableField(exist = false)
    private Integer unreadCount;

    /**
     * 会话对象信息（私聊时使用，非数据库字段）
     */
    @TableField(exist = false)
    private WxUser targetUser;

    /**
     * 会话类型常量
     */
    public static final int TYPE_PRIVATE = 1; // 私聊
    public static final int TYPE_FAMILY = 2; // 家庭群聊
}
