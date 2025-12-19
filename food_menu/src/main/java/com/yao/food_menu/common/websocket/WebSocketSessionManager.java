package com.yao.food_menu.common.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket会话管理器
 * 管理所有在线用户的WebSocket连接
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    /**
     * 用户ID -> WebSocket会话 映射
     */
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * 添加会话
     */
    public void addSession(Long userId, WebSocketSession session) {
        // 如果该用户已有连接，先关闭旧连接
        WebSocketSession oldSession = sessions.get(userId);
        if (oldSession != null && oldSession.isOpen()) {
            try {
                oldSession.close();
                log.info("关闭用户{}的旧WebSocket连接", userId);
            } catch (IOException e) {
                log.error("关闭旧WebSocket连接失败", e);
            }
        }
        sessions.put(userId, session);
        log.info("用户{}的WebSocket连接已添加, 当前在线人数: {}", userId, sessions.size());
    }

    /**
     * 移除会话
     */
    public void removeSession(Long userId) {
        sessions.remove(userId);
        log.info("用户{}的WebSocket连接已移除, 当前在线人数: {}", userId, sessions.size());
    }

    /**
     * 获取会话
     */
    public WebSocketSession getSession(Long userId) {
        return sessions.get(userId);
    }

    /**
     * 检查用户是否在线
     */
    public boolean isOnline(Long userId) {
        WebSocketSession session = sessions.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 向指定用户发送消息
     */
    public boolean sendToUser(Long userId, String message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                return true;
            } catch (IOException e) {
                log.error("向用户{}发送消息失败", userId, e);
                // 发送失败，移除该会话
                removeSession(userId);
            }
        }
        return false;
    }

    /**
     * 向多个用户发送消息
     */
    public void sendToUsers(Iterable<Long> userIds, String message) {
        for (Long userId : userIds) {
            sendToUser(userId, message);
        }
    }

    /**
     * 获取在线用户数量
     */
    public int getOnlineCount() {
        return sessions.size();
    }
}
