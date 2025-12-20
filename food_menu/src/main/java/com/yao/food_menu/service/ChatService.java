package com.yao.food_menu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yao.food_menu.dto.ChatConversationDTO;
import com.yao.food_menu.dto.ChatMessageDTO;
import com.yao.food_menu.dto.CreateConversationDTO;
import com.yao.food_menu.dto.SendMessageDTO;
import com.yao.food_menu.entity.ChatConversation;
import com.yao.food_menu.entity.ChatMessage;

import java.util.List;

/**
 * 聊天服务接口
 */
public interface ChatService {

    /**
     * 获取用户的会话列表
     * 
     * @param wxUserId 用户ID
     * @return 会话列表
     */
    List<ChatConversationDTO> getConversationList(Long wxUserId);

    /**
     * 创建会话
     * 
     * @param wxUserId 创建者用户ID
     * @param dto      创建参数
     * @return 会话
     */
    ChatConversation createConversation(Long wxUserId, CreateConversationDTO dto);

    /**
     * 获取或创建私聊会话
     * 
     * @param currentUserId 当前用户ID
     * @param targetUserId  目标用户ID
     * @return 会话
     */
    ChatConversation getOrCreatePrivateConversation(Long currentUserId, Long targetUserId);

    /**
     * 获取或创建家庭群聊会话
     * 
     * @param familyId   家庭ID
     * @param familyName 家庭名称
     * @return 会话
     */
    ChatConversation getOrCreateFamilyConversation(Long familyId, String familyName);

    /**
     * 删除会话（对当前用户隐藏）
     * 
     * @param conversationId 会话ID
     * @param wxUserId       用户ID
     */
    void deleteConversation(Long conversationId, Long wxUserId);

    /**
     * 获取会话详情
     * 
     * @param conversationId 会话ID
     * @param wxUserId       用户ID
     * @return 会话详情
     */
    ChatConversationDTO getConversationDetail(Long conversationId, Long wxUserId);

    /**
     * 分页获取消息历史
     * 
     * @param conversationId 会话ID
     * @param wxUserId       用户ID
     * @param page           页码
     * @param size           每页数量
     * @return 消息列表
     */
    IPage<ChatMessageDTO> getMessageHistory(Long conversationId, Long wxUserId, int page, int size);

    /**
     * 获取指定消息ID之前的消息
     * 
     * @param conversationId 会话ID
     * @param beforeId       消息ID
     * @param limit          数量
     * @param wxUserId       当前用户ID
     * @return 消息列表
     */
    List<ChatMessageDTO> getMessagesBeforeId(Long conversationId, Long beforeId, int limit, Long wxUserId);

    /**
     * 发送消息
     * 
     * @param wxUserId 发送者用户ID
     * @param dto      消息参数
     * @return 消息
     */
    ChatMessage sendMessage(Long wxUserId, SendMessageDTO dto);

    /**
     * 撤回消息
     * 
     * @param messageId 消息ID
     * @param wxUserId  用户ID
     * @return 是否成功
     */
    boolean revokeMessage(Long messageId, Long wxUserId);

    /**
     * 标记消息已读
     * 
     * @param conversationId 会话ID
     * @param messageId      消息ID
     * @param wxUserId       用户ID
     */
    void markAsRead(Long conversationId, Long messageId, Long wxUserId);

    /**
     * 获取用户总未读消息数
     * 
     * @param wxUserId 用户ID
     * @return 未读消息数
     */
    int getTotalUnreadCount(Long wxUserId);

    /**
     * 检查用户是否是会话成员
     * 
     * @param conversationId 会话ID
     * @param wxUserId       用户ID
     * @return 是否是成员
     */
    boolean isMember(Long conversationId, Long wxUserId);

    /**
     * 获取会话中其他成员的用户ID
     * 
     * @param conversationId 会话ID
     * @param excludeUserId  排除的用户ID
     * @return 用户ID列表
     */
    List<Long> getOtherMemberIds(Long conversationId, Long excludeUserId);

    /**
     * 将消息转换为DTO
     * 
     * @param message       消息实体
     * @param currentUserId 当前用户ID
     * @return 消息DTO
     */
    ChatMessageDTO convertToDTO(ChatMessage message, Long currentUserId);
}
