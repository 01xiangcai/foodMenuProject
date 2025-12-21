package com.yao.food_menu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI对话历史实体类
 */
@Data
@TableName("ai_chat_history")
public class AiChatHistory {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 微信用户ID
     */
    private Long wxUserId;

    /**
     * 家庭ID
     */
    private Long familyId;

    /**
     * 角色: user(用户) 或 assistant(AI助手)
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
