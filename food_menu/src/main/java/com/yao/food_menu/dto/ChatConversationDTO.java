package com.yao.food_menu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话列表响应DTO
 */
@Data
public class ChatConversationDTO {

    /**
     * 会话ID
     */
    private Long id;

    /**
     * 会话类型: 1-私聊, 2-家庭群聊
     */
    private Integer type;

    /**
     * 会话名称
     */
    private String name;

    /**
     * 会话头像
     */
    private String avatar;

    /**
     * 未读消息数
     */
    private Integer unreadCount;

    /**
     * 最后消息内容
     */
    /**
     * 最后消息内容
     */
    private String lastMessageContent;

    /**
     * 最后消息ID
     */
    private Long lastMessageId;

    /**
     * 最后消息时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMessageTime;

    /**
     * 最后消息时间格式化（用于显示：刚刚、5分钟前、昨天等）
     */
    private String lastMessageTimeFormatted;

    /**
     * 家庭ID
     */
    private Long familyId;

    /**
     * 对方用户ID（私聊时使用）
     */
    private Long targetUserId;

    /**
     * 对方用户昵称（私聊时使用）
     */
    private String targetUserName;

    /**
     * 是否免打扰
     */
    private Boolean muted;

    /**
     * 群成员头像列表（用于生成九宫格头像，最多9个）
     */
    private java.util.List<String> memberAvatars;
}
