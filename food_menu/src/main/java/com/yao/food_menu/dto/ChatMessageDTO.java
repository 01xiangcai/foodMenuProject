package com.yao.food_menu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息响应DTO
 */
@Data
public class ChatMessageDTO {

    /**
     * 消息ID
     */
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
     * 发送者昵称
     */
    private String senderName;

    /**
     * 发送者头像
     */
    private String senderAvatar;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型
     */
    private Integer type;

    /**
     * 扩展信息
     */
    private String extra;

    /**
     * 消息状态
     */
    private Integer status;

    /**
     * 回复的消息ID
     */
    private Long replyToId;

    /**
     * 被回复的消息内容预览
     */
    private String replyToContent;

    /**
     * 发送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 是否是自己发送的消息
     */
    private Boolean isSelf;
}
