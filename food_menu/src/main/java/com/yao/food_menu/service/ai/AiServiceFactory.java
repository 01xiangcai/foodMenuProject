package com.yao.food_menu.service.ai;

import com.yao.food_menu.config.AiConfig;
import com.yao.food_menu.enums.AiProvider;
import com.yao.food_menu.service.ai.impl.OllamaAiServiceImpl;
import com.yao.food_menu.service.ai.impl.QwenAiServiceImpl;
import com.yao.food_menu.service.ai.impl.SiliconFlowAiServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * AI服务工厂
 * 根据配置返回对应的AI服务实现
 * 支持运行时动态切换服务提供商
 */
@Slf4j
@Component
public class AiServiceFactory {

    @Autowired
    private AiConfig aiConfig;

    @Autowired(required = false)
    private SiliconFlowAiServiceImpl siliconFlowAiService;

    @Autowired(required = false)
    private QwenAiServiceImpl qwenAiService;

    @Autowired(required = false)
    private OllamaAiServiceImpl ollamaAiService;

    /**
     * 获取当前配置的AI服务实例
     */
    public AiService getAiService() {
        AiProvider provider = aiConfig.getProviderEnum();
        log.info("使用AI服务提供商: {}", provider.getName());

        switch (provider) {
            case SILICON_FLOW:
                if (siliconFlowAiService == null) {
                    throw new IllegalStateException("硅基流动AI服务未初始化");
                }
                return siliconFlowAiService;

            case QWEN:
                if (qwenAiService == null) {
                    throw new IllegalStateException("通义千问AI服务未初始化");
                }
                return qwenAiService;

            case OLLAMA:
                if (ollamaAiService == null) {
                    throw new IllegalStateException("Ollama AI服务未初始化");
                }
                return ollamaAiService;

            default:
                throw new IllegalArgumentException("不支持的AI服务提供商: " + provider.getName());
        }
    }

    /**
     * 获取指定的AI服务实例(用于高级场景,如A/B测试)
     */
    public AiService getAiService(AiProvider provider) {
        log.info("手动指定AI服务提供商: {}", provider.getName());

        switch (provider) {
            case SILICON_FLOW:
                return siliconFlowAiService;
            case QWEN:
                return qwenAiService;
            case OLLAMA:
                return ollamaAiService;
            default:
                throw new IllegalArgumentException("不支持的AI服务提供商: " + provider.getName());
        }
    }
}
