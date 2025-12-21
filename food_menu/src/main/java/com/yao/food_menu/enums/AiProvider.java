package com.yao.food_menu.enums;

/**
 * AI服务提供商枚举
 * 支持多种AI服务商,方便后期切换
 */
public enum AiProvider {
    /**
     * 硅基流动 - 免费,兼容OpenAI格式
     */
    SILICON_FLOW("silicon_flow", "硅基流动"),

    /**
     * 通义千问 - 阿里云官方
     */
    QWEN("qwen", "通义千问"),

    /**
     * 百度文心一言
     */
    BAIDU_WENXIN("baidu_wenxin", "百度文心一言"),

    /**
     * 智谱AI
     */
    ZHIPU_AI("zhipu_ai", "智谱AI"),

    /**
     * 本地Ollama部署
     */
    OLLAMA("ollama", "本地Ollama");

    private final String code;
    private final String name;

    AiProvider(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据code获取枚举
     */
    public static AiProvider fromCode(String code) {
        for (AiProvider provider : values()) {
            if (provider.code.equals(code)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("不支持的AI服务提供商: " + code);
    }
}
