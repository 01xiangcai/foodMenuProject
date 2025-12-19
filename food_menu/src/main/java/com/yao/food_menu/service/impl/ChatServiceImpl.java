package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yao.food_menu.common.exception.BusinessException;
import com.yao.food_menu.dto.ChatConversationDTO;
import com.yao.food_menu.dto.ChatMessageDTO;
import com.yao.food_menu.dto.CreateConversationDTO;
import com.yao.food_menu.dto.SendMessageDTO;
import com.yao.food_menu.entity.ChatConversation;
import com.yao.food_menu.entity.ChatConversationMember;
import com.yao.food_menu.entity.ChatMessage;
import com.yao.food_menu.entity.WxUser;
import com.yao.food_menu.mapper.ChatConversationMapper;
import com.yao.food_menu.mapper.ChatConversationMemberMapper;
import com.yao.food_menu.mapper.ChatMessageMapper;
import com.yao.food_menu.mapper.WxUserMapper;
import com.yao.food_menu.service.ChatService;
import com.yao.food_menu.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatConversationMapper conversationMapper;
    private final ChatConversationMemberMapper memberMapper;
    private final ChatMessageMapper messageMapper;
    private final WxUserMapper wxUserMapper;
    private final SystemConfigService systemConfigService;

    @Override
    public List<ChatConversationDTO> getConversationList(Long wxUserId) {
        List<ChatConversation> conversations = conversationMapper.selectByWxUserId(wxUserId);
        return conversations.stream()
                .map(conv -> convertConversationToDTO(conv, wxUserId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatConversation createConversation(Long wxUserId, CreateConversationDTO dto) {
        if (dto.getType() == ChatConversation.TYPE_PRIVATE) {
            return getOrCreatePrivateConversation(wxUserId, dto.getTargetUserId());
        } else if (dto.getType() == ChatConversation.TYPE_FAMILY) {
            return getOrCreateFamilyConversation(dto.getFamilyId(), dto.getName());
        }
        throw new BusinessException("不支持的会话类型");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatConversation getOrCreatePrivateConversation(Long currentUserId, Long targetUserId) {
        // 检查是否已存在私聊会话
        ChatConversation existing = conversationMapper.findPrivateConversation(currentUserId, targetUserId);
        if (existing != null) {
            return existing;
        }

        // 创建新的私聊会话
        ChatConversation conversation = new ChatConversation();
        conversation.setType(ChatConversation.TYPE_PRIVATE);
        conversation.setCreateTime(LocalDateTime.now());
        conversation.setUpdateTime(LocalDateTime.now());
        conversationMapper.insert(conversation);

        // 添加两个成员
        addMember(conversation.getId(), currentUserId, ChatConversationMember.ROLE_MEMBER);
        addMember(conversation.getId(), targetUserId, ChatConversationMember.ROLE_MEMBER);

        return conversation;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatConversation getOrCreateFamilyConversation(Long familyId, String familyName) {
        // 检查是否已存在家庭群聊
        ChatConversation existing = conversationMapper.findFamilyConversation(familyId);
        if (existing != null) {
            return existing;
        }

        // 创建新的家庭群聊
        ChatConversation conversation = new ChatConversation();
        conversation.setType(ChatConversation.TYPE_FAMILY);
        conversation.setFamilyId(familyId);
        conversation.setName(familyName + " 家庭群");
        conversation.setCreateTime(LocalDateTime.now());
        conversation.setUpdateTime(LocalDateTime.now());
        conversationMapper.insert(conversation);

        return conversation;
    }

    @Override
    public ChatConversationDTO getConversationDetail(Long conversationId, Long wxUserId) {
        ChatConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException("会话不存在");
        }
        if (!isMember(conversationId, wxUserId)) {
            throw new BusinessException("您不是该会话的成员");
        }
        return convertConversationToDTO(conversation, wxUserId);
    }

    @Override
    public IPage<ChatMessageDTO> getMessageHistory(Long conversationId, Long wxUserId, int page, int size) {
        if (!isMember(conversationId, wxUserId)) {
            throw new BusinessException("您不是该会话的成员");
        }

        Page<ChatMessage> pageParam = new Page<>(page, size);
        IPage<ChatMessage> messagePage = messageMapper.selectByConversationId(pageParam, conversationId);

        // 转换为DTO
        IPage<ChatMessageDTO> dtoPage = messagePage.convert(msg -> convertToDTO(msg, wxUserId));
        return dtoPage;
    }

    @Override
    public List<ChatMessageDTO> getMessagesBeforeId(Long conversationId, Long beforeId, int limit, Long wxUserId) {
        if (!isMember(conversationId, wxUserId)) {
            throw new BusinessException("您不是该会话的成员");
        }

        List<ChatMessage> messages = messageMapper.selectBeforeId(conversationId, beforeId, limit);
        return messages.stream()
                .map(msg -> convertToDTO(msg, wxUserId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessage sendMessage(Long wxUserId, SendMessageDTO dto) {
        if (!isMember(dto.getConversationId(), wxUserId)) {
            throw new BusinessException("您不是该会话的成员");
        }

        // 创建消息
        ChatMessage message = new ChatMessage();
        message.setConversationId(dto.getConversationId());
        message.setSenderId(wxUserId);
        message.setContent(dto.getContent());
        message.setType(dto.getType() != null ? dto.getType() : ChatMessage.TYPE_TEXT);
        message.setExtra(dto.getExtra());
        message.setReplyToId(dto.getReplyToId());
        message.setStatus(ChatMessage.STATUS_NORMAL);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
        messageMapper.insert(message);

        // 更新会话最后消息信息
        ChatConversation conversation = conversationMapper.selectById(dto.getConversationId());
        conversation.setLastMessageId(message.getId());
        conversation.setLastMessageTime(message.getCreateTime());
        conversation.setLastMessageContent(truncateContent(dto.getContent()));
        conversation.setUpdateTime(LocalDateTime.now());
        conversationMapper.updateById(conversation);

        // 增加其他成员的未读数
        memberMapper.incrementUnreadCount(dto.getConversationId(), wxUserId);

        return message;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean revokeMessage(Long messageId, Long wxUserId) {
        ChatMessage message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        if (!message.getSenderId().equals(wxUserId)) {
            throw new BusinessException("只能撤回自己发送的消息");
        }

        // 检查撤回时限
        int revokeTimeLimit = getRevokeTimeLimit();
        Duration duration = Duration.between(message.getCreateTime(), LocalDateTime.now());
        if (duration.toMinutes() > revokeTimeLimit) {
            throw new BusinessException("消息发送超过" + revokeTimeLimit + "分钟，无法撤回");
        }

        int rows = messageMapper.revokeMessage(messageId, wxUserId);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long conversationId, Long messageId, Long wxUserId) {
        memberMapper.updateLastReadMessage(conversationId, wxUserId, messageId);
    }

    @Override
    public int getTotalUnreadCount(Long wxUserId) {
        List<ChatConversation> conversations = conversationMapper.selectByWxUserId(wxUserId);
        int totalUnread = 0;
        for (ChatConversation conv : conversations) {
            ChatConversationMember member = memberMapper.selectByConversationAndUser(conv.getId(), wxUserId);
            if (member != null) {
                totalUnread += member.getUnreadCount() != null ? member.getUnreadCount() : 0;
            }
        }
        return totalUnread;
    }

    @Override
    public boolean isMember(Long conversationId, Long wxUserId) {
        ChatConversationMember member = memberMapper.selectByConversationAndUser(conversationId, wxUserId);
        return member != null;
    }

    @Override
    public List<Long> getOtherMemberIds(Long conversationId, Long excludeUserId) {
        return memberMapper.selectOtherMemberIds(conversationId, excludeUserId);
    }

    @Override
    public ChatMessageDTO convertToDTO(ChatMessage message, Long currentUserId) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversationId());
        dto.setSenderId(message.getSenderId());
        dto.setContent(message.getContent());
        dto.setType(message.getType());
        dto.setExtra(message.getExtra());
        dto.setStatus(message.getStatus());
        dto.setReplyToId(message.getReplyToId());
        dto.setCreateTime(message.getCreateTime());
        dto.setIsSelf(message.getSenderId().equals(currentUserId));

        // 获取发送者信息（使用不带family_id过滤的查询，避免WebSocket场景下FamilyContext为空的问题）
        WxUser sender = wxUserMapper.selectByIdWithoutFamilyFilter(message.getSenderId());
        if (sender != null) {
            dto.setSenderName(sender.getNickname());
            dto.setSenderAvatar(sender.getAvatar());
        }

        // 获取被回复的消息内容
        if (message.getReplyToId() != null) {
            ChatMessage replyTo = messageMapper.selectById(message.getReplyToId());
            if (replyTo != null) {
                dto.setReplyToContent(truncateContent(replyTo.getContent()));
            }
        }

        return dto;
    }

    /**
     * 将会话转换为DTO
     */
    private ChatConversationDTO convertConversationToDTO(ChatConversation conversation, Long wxUserId) {
        ChatConversationDTO dto = new ChatConversationDTO();
        dto.setId(conversation.getId());
        dto.setType(conversation.getType());
        dto.setFamilyId(conversation.getFamilyId());
        dto.setLastMessageContent(conversation.getLastMessageContent());
        dto.setLastMessageTime(conversation.getLastMessageTime());
        dto.setLastMessageTimeFormatted(formatMessageTime(conversation.getLastMessageTime()));

        // 获取当前用户在会话中的信息
        ChatConversationMember member = memberMapper.selectByConversationAndUser(conversation.getId(), wxUserId);
        if (member != null) {
            dto.setUnreadCount(member.getUnreadCount() != null ? member.getUnreadCount() : 0);
            dto.setMuted(member.getMuted() != null && member.getMuted() == 1);
        }

        if (conversation.getType() == ChatConversation.TYPE_PRIVATE) {
            // 私聊：获取对方信息
            List<Long> otherIds = getOtherMemberIds(conversation.getId(), wxUserId);
            if (!otherIds.isEmpty()) {
                WxUser targetUser = wxUserMapper.selectById(otherIds.get(0));
                if (targetUser != null) {
                    dto.setName(targetUser.getNickname());
                    dto.setAvatar(targetUser.getAvatar());
                    dto.setTargetUserId(targetUser.getId());
                    dto.setTargetUserName(targetUser.getNickname());
                }
            }
        } else {
            // 群聊：使用会话名称和头像
            dto.setName(conversation.getName());
            dto.setAvatar(conversation.getAvatar());
        }

        return dto;
    }

    /**
     * 添加会话成员
     */
    private void addMember(Long conversationId, Long wxUserId, int role) {
        ChatConversationMember member = new ChatConversationMember();
        member.setConversationId(conversationId);
        member.setWxUserId(wxUserId);
        member.setRole(role);
        member.setLastReadMessageId(0L);
        member.setUnreadCount(0);
        member.setMuted(0);
        member.setJoinTime(LocalDateTime.now());
        member.setCreateTime(LocalDateTime.now());
        member.setUpdateTime(LocalDateTime.now());
        memberMapper.insert(member);
    }

    /**
     * 获取撤回时限（分钟）
     */
    private int getRevokeTimeLimit() {
        try {
            String value = systemConfigService.getConfigValue("chat_revoke_time_limit");
            return value != null ? Integer.parseInt(value) : 2;
        } catch (Exception e) {
            return 2; // 默认2分钟
        }
    }

    /**
     * 截断消息内容用于预览
     */
    private String truncateContent(String content) {
        if (content == null)
            return "";
        return content.length() > 50 ? content.substring(0, 50) + "..." : content;
    }

    /**
     * 格式化消息时间
     */
    private String formatMessageTime(LocalDateTime time) {
        if (time == null)
            return "";

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(time, now);

        if (duration.toMinutes() < 1) {
            return "刚刚";
        } else if (duration.toHours() < 1) {
            return duration.toMinutes() + "分钟前";
        } else if (duration.toHours() < 24 && time.toLocalDate().equals(now.toLocalDate())) {
            return time.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else if (time.toLocalDate().equals(now.toLocalDate().minusDays(1))) {
            return "昨天 " + time.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else if (time.getYear() == now.getYear()) {
            return time.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
        } else {
            return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
    }
}
