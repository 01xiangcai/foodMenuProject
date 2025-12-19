package com.yao.food_menu.common.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * WebSocket配置类
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final WebSocketAuthInterceptor authInterceptor;

    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler, WebSocketAuthInterceptor authInterceptor) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册WebSocket处理器
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(authInterceptor)
                .setAllowedOrigins("*"); // 生产环境应配置具体域名
    }

    /**
     * WebSocket容器配置
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(8192); // 文本消息最大8KB
        container.setMaxBinaryMessageBufferSize(8192); // 二进制消息最大8KB
        container.setMaxSessionIdleTimeout(60000L); // 会话空闲超时60秒
        return container;
    }
}
