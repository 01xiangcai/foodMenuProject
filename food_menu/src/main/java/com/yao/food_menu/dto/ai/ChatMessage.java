package com.yao.food_menu.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    /**
     * 角色: user(用户) 或 assistant(AI助手)
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;
}
