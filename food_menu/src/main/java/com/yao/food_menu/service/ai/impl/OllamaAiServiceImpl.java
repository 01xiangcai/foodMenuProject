package com.yao.food_menu.service.ai.impl;

import com.yao.food_menu.dto.ai.ChatMessage;
import com.yao.food_menu.dto.ai.DishRecommendation;
import com.yao.food_menu.dto.ai.OrderParseResult;
import com.yao.food_menu.service.ai.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Ollama本地AI服务实现(预留)
 * 当需要本地部署AI模型时,实现此类的具体逻辑
 */
@Slf4j
@Service
public class OllamaAiServiceImpl implements AiService {

    @Override
    public String chat(String message, List<ChatMessage> history) {
        log.warn("Ollama AI服务尚未实现");
        return "Ollama AI服务尚未配置,请联系管理员";
    }

    @Override
    public List<DishRecommendation> recommendDishes(Long familyId, Long userId, String mealPeriod,
            String preferences, Integer count) {
        log.warn("Ollama AI服务尚未实现");
        return new ArrayList<>();
    }

    @Override
    public OrderParseResult parseOrderText(String text, Long familyId) {
        log.warn("Ollama AI服务尚未实现");
        OrderParseResult result = new OrderParseResult();
        result.setSuccess(false);
        result.setErrorMessage("Ollama AI服务尚未配置");
        return result;
    }

    @Override
    public String generateWeeklyMenu(Long familyId, String preferences) {
        log.warn("Ollama AI服务尚未实现");
        return "{\"error\": \"Ollama AI服务尚未配置\"}";
    }
}
