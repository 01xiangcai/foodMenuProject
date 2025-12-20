package com.yao.food_menu.common.websocket;

import lombok.Data;

/**
 * WebSocket消息对象
 */
@Data
public class WebSocketMessage {

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息数据
     */
    private Object data;

    /**
     * 消息类型常量
     */
    public static final String TYPE_PING = "PING"; // 心跳请求
    public static final String TYPE_PONG = "PONG"; // 心跳响应
    public static final String TYPE_SEND_MESSAGE = "SEND_MESSAGE"; // 发送消息
    public static final String TYPE_NEW_MESSAGE = "NEW_MESSAGE"; // 新消息通知
    public static final String TYPE_MESSAGE_REVOKED = "MESSAGE_REVOKED"; // 消息撤回通知
    public static final String TYPE_READ_ACK = "READ_ACK"; // 已读确认
    public static final String TYPE_ERROR = "ERROR"; // 错误消息
    public static final String TYPE_CONNECTED = "CONNECTED"; // 连接成功
    public static final String TYPE_SYSTEM_NOTIFICATION = "SYSTEM_NOTIFICATION"; // 系统通知

    public WebSocketMessage() {
    }

    public WebSocketMessage(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public static WebSocketMessage pong() {
        return new WebSocketMessage(TYPE_PONG, null);
    }

    public static WebSocketMessage connected(Long userId) {
        return new WebSocketMessage(TYPE_CONNECTED, userId);
    }

    public static WebSocketMessage error(String message) {
        return new WebSocketMessage(TYPE_ERROR, message);
    }

    public static WebSocketMessage newMessage(Object messageData) {
        return new WebSocketMessage(TYPE_NEW_MESSAGE, messageData);
    }

    public static WebSocketMessage messageRevoked(Long messageId, Long conversationId) {
        return new WebSocketMessage(TYPE_MESSAGE_REVOKED,
                new RevokeData(messageId, conversationId));
    }

    @Data
    public static class RevokeData {
        private Long messageId;
        private Long conversationId;

        public RevokeData(Long messageId, Long conversationId) {
            this.messageId = messageId;
            this.conversationId = conversationId;
        }
    }
}
