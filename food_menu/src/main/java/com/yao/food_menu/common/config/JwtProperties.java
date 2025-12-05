package com.yao.food_menu.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT密钥
     */
    private String secret;

    /**
     * Token过期时间（毫秒），默认7天
     */
    private Long expirationTime = 7 * 24 * 60 * 60 * 1000L;
}

