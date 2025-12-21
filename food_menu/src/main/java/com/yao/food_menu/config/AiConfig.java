package com.yao.food_menu.config;

import com.yao.food_menu.enums.AiProvider;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI服务配置类
 * 从application.yml读取AI相关配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiConfig {

    /**
     * 当前使用的AI服务提供商
     */
    private String provider;

    /**
     * 硅基流动配置
     */
    private SiliconFlowConfig siliconFlow;

    /**
     * 通义千问配置
     */
    private QwenConfig qwen;

    /**
     * Ollama配置
     */
    private OllamaConfig ollama;

    /**
     * 获取当前AI服务提供商枚举
     */
    public AiProvider getProviderEnum() {
        return AiProvider.fromCode(provider);
    }

    /**
     * 硅基流动配置
     */
    @Data
    public static class SiliconFlowConfig {
        private String apiKey;
        private String baseUrl;
        private String model;
        private Integer maxTokens;
        private Double temperature;
        private Integer timeout;
    }

    /**
     * 通义千问配置
     */
    @Data
    public static class QwenConfig {
        private String apiKey;
        private String baseUrl;
        private String model;
    }

    /**
     * Ollama配置
     */
    @Data
    public static class OllamaConfig {
        private String baseUrl;
        private String model;
    }
}
