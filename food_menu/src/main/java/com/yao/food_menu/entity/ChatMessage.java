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
 * 聊天消息实体类
 */
@Data
@TableName("chat_message")
public class ChatMessage implements Serializable {

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
     * 发送者ID
     */
    private Long senderId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型: 1-文本, 2-图片, 3-语音, 4-表情, 5-系统消息
     */
    private Integer type;

    /**
     * 扩展信息（图片尺寸、语音时长等）JSON格式
     */
    private String extra;

    /**
     * 消息状态: 0-正常, 1-已撤回
     */
    private Integer status;

    /**
     * 回复的消息ID（用于引用回复）
     */
    private Long replyToId;

    /**
     * 发送时间
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
     * 发送者信息（非数据库字段）
     */
    @TableField(exist = false)
    private WxUser sender;

    /**
     * 被回复的消息（非数据库字段）
     */
    @TableField(exist = false)
    private ChatMessage replyToMessage;

    /**
     * 消息类型常量
     */
    public static final int TYPE_TEXT = 1; // 文本
    public static final int TYPE_IMAGE = 2; // 图片
    public static final int TYPE_VOICE = 3; // 语音
    public static final int TYPE_EMOJI = 4; // 表情
    public static final int TYPE_SYSTEM = 5; // 系统消息

    /**
     * 消息状态常量
     */
    public static final int STATUS_NORMAL = 0; // 正常
    public static final int STATUS_REVOKED = 1; // 已撤回
}
