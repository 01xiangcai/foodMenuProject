package com.yao.food_menu.dto;

import lombok.Data;

/**
 * 发送消息请求DTO
 */
@Data
public class SendMessageDTO {

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型: 1-文本, 2-图片, 3-语音, 4-表情
     */
    private Integer type = 1;

    /**
     * 扩展信息（JSON格式）
     */
    private String extra;

    /**
     * 回复的消息ID
     */
    private Long replyToId;
}
