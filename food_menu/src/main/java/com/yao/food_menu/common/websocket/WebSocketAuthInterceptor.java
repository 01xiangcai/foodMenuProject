package com.yao.food_menu.common.websocket;

import com.yao.food_menu.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket握手认证拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;

            // 从URL参数获取token
            String token = servletRequest.getServletRequest().getParameter("token");

            if (token == null || token.isEmpty()) {
                log.warn("WebSocket连接失败: 缺少token参数");
                return false;
            }

            try {
                // 验证token并获取用户ID
                Long userId = jwtUtil.getUserId(token);
                if (userId == null) {
                    log.warn("WebSocket连接失败: 无效的token");
                    return false;
                }

                // 将用户ID存入attributes，后续Handler可以获取
                attributes.put("userId", userId);
                log.info("WebSocket连接认证成功, userId: {}", userId);
                return true;
            } catch (Exception e) {
                log.warn("WebSocket连接失败: token验证异常 - {}", e.getMessage());
                return false;
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Exception exception) {
        // 握手后回调，可用于日志记录
        if (exception != null) {
            log.error("WebSocket握手异常", exception);
        }
    }
}
