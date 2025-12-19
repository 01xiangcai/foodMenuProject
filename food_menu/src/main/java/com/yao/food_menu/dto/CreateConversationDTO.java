package com.yao.food_menu.dto;

import lombok.Data;
import java.util.List;

/**
 * 创建会话请求DTO
 */
@Data
public class CreateConversationDTO {

    /**
     * 会话类型: 1-私聊, 2-家庭群聊
     */
    private Integer type;

    /**
     * 目标用户ID（私聊时使用）
     */
    private Long targetUserId;

    /**
     * 家庭ID（家庭群聊时使用）
     */
    private Long familyId;

    /**
     * 会话名称（群聊名）
     */
    private String name;

    /**
     * 成员ID列表（群聊时使用）
     */
    private List<Long> memberIds;
}
