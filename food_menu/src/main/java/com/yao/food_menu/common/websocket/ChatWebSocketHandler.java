package com.yao.food_menu.common.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yao.food_menu.common.context.FamilyContext;
import com.yao.food_menu.common.util.ImageUrlUtil;
import com.yao.food_menu.dto.ChatMessageDTO;
import com.yao.food_menu.dto.SendMessageDTO;
import com.yao.food_menu.entity.ChatMessage;
import com.yao.food_menu.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;

/**
 * 聊天WebSocket处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final ChatService chatService;
    private final ObjectMapper objectMapper;
    private final ImageUrlUtil imageUrlUtil;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserId(session);
        if (userId != null) {
            sessionManager.addSession(userId, session);
            // 发送连接成功消息
            sendMessage(session, WebSocketMessage.connected(userId));
            log.info("WebSocket连接建立, userId: {}, sessionId: {}", userId, session.getId());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = getUserId(session);
        if (userId == null) {
            sendMessage(session, WebSocketMessage.error("未认证的连接"));
            return;
        }

        String payload = message.getPayload();
        log.debug("收到WebSocket消息, userId: {}, payload: {}", userId, payload);

        // 设置FamilyContext，确保后续的数据库查询能正确应用家庭过滤
        Long familyId = getFamilyId(session);
        try {
            if (familyId != null) {
                FamilyContext.setFamilyId(familyId);
                FamilyContext.setWxUserId(userId);
            }

            JsonNode jsonNode = objectMapper.readTree(payload);
            String type = jsonNode.get("type").asText();

            switch (type) {
                case WebSocketMessage.TYPE_PING:
                    // 心跳响应
                    sendMessage(session, WebSocketMessage.pong());
                    break;

                case WebSocketMessage.TYPE_SEND_MESSAGE:
                    // 处理发送消息
                    handleSendMessage(userId, jsonNode.get("data"));
                    break;

                case WebSocketMessage.TYPE_READ_ACK:
                    // 处理已读确认
                    handleReadAck(userId, jsonNode.get("data"));
                    break;

                default:
                    log.warn("未知的消息类型: {}", type);
                    sendMessage(session, WebSocketMessage.error("未知的消息类型: " + type));
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
            sendMessage(session, WebSocketMessage.error("消息处理失败: " + e.getMessage()));
        } finally {
            // 清除FamilyContext，避免线程污染
            FamilyContext.clear();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserId(session);
        if (userId != null) {
            sessionManager.removeSession(userId);
            log.info("WebSocket连接关闭, userId: {}, status: {}", userId, status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = getUserId(session);
        log.error("WebSocket传输错误, userId: {}", userId, exception);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    /**
     * 处理发送消息
     */
    private void handleSendMessage(Long senderId, JsonNode data) {
        try {
            SendMessageDTO dto = objectMapper.treeToValue(data, SendMessageDTO.class);
            log.info("处理发送消息, senderId: {}, conversationId: {}, content: {}",
                    senderId, dto.getConversationId(), dto.getContent());

            // 保存消息
            ChatMessage message = chatService.sendMessage(senderId, dto);
            log.info("消息已保存, messageId: {}", message.getId());

            // 转换为DTO
            ChatMessageDTO messageDTO = chatService.convertToDTO(message, senderId);
            // 处理头像URL
            processMessageAvatar(messageDTO);

            // 推送给会话中的其他成员
            List<Long> otherMemberIds = chatService.getOtherMemberIds(dto.getConversationId(), senderId);
            log.info("其他成员列表: {}, 在线状态: {}", otherMemberIds,
                    otherMemberIds.stream().map(id -> id + ":" + sessionManager.isOnline(id)).toList());

            for (Long memberId : otherMemberIds) {
                // 为每个接收者创建DTO（isSelf=false）
                ChatMessageDTO receiverDTO = chatService.convertToDTO(message, memberId);
                // 处理头像URL
                processMessageAvatar(receiverDTO);
                String jsonMessage = toJson(WebSocketMessage.newMessage(receiverDTO));
                log.info("向用户{}推送消息: {}", memberId, jsonMessage);
                boolean sent = sessionManager.sendToUser(memberId, jsonMessage);
                log.info("推送结果: {}", sent ? "成功" : "失败(用户不在线或发送失败)");
            }

            // 给发送者确认消息发送成功
            WebSocketSession senderSession = sessionManager.getSession(senderId);
            if (senderSession != null) {
                messageDTO.setIsSelf(true);
                sendMessage(senderSession, WebSocketMessage.newMessage(messageDTO));
            }

            log.info("消息发送成功, messageId: {}, conversationId: {}, senderId: {}",
                    message.getId(), dto.getConversationId(), senderId);
        } catch (Exception e) {
            log.error("发送消息失败", e);
            WebSocketSession session = sessionManager.getSession(senderId);
            if (session != null) {
                sendMessage(session, WebSocketMessage.error("发送消息失败: " + e.getMessage()));
            }
        }
    }

    /**
     * 处理已读确认
     */
    private void handleReadAck(Long userId, JsonNode data) {
        try {
            Long conversationId = data.get("conversationId").asLong();
            Long messageId = data.get("messageId").asLong();
            chatService.markAsRead(conversationId, messageId, userId);
            log.debug("已读确认, userId: {}, conversationId: {}, messageId: {}", userId, conversationId, messageId);
        } catch (Exception e) {
            log.error("处理已读确认失败", e);
        }
    }

    /**
     * 通知消息撤回
     */
    public void notifyMessageRevoked(Long messageId, Long conversationId, Long senderId) {
        List<Long> memberIds = chatService.getOtherMemberIds(conversationId, senderId);
        String message = toJson(WebSocketMessage.messageRevoked(messageId, conversationId));
        sessionManager.sendToUsers(memberIds, message);
    }

    /**
     * 获取用户ID
     */
    private Long getUserId(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        return userId != null ? (Long) userId : null;
    }

    /**
     * 获取家庭ID
     */
    private Long getFamilyId(WebSocketSession session) {
        Object familyId = session.getAttributes().get("familyId");
        return familyId != null ? (Long) familyId : null;
    }

    /**
     * 发送消息到WebSocket会话
     */
    private void sendMessage(WebSocketSession session, WebSocketMessage message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(toJson(message)));
            }
        } catch (Exception e) {
            log.error("发送WebSocket消息失败", e);
        }
    }

    /**
     * 对象转JSON
     */
    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("JSON序列化失败", e);
            return "{}";
        }
    }

    /**
     * 处理消息DTO中的头像URL
     */
    private void processMessageAvatar(ChatMessageDTO dto) {
        if (dto != null) {
            String originalAvatar = dto.getSenderAvatar();
            String processedAvatar = imageUrlUtil.processAvatarUrl(originalAvatar);
            log.info("处理头像URL: 原始={}, 处理后={}", originalAvatar, processedAvatar);
            dto.setSenderAvatar(processedAvatar);
        }
    }
}
