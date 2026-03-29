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
 * 通义千问AI服务实现(预留)
 * 当需要切换到通义千问时,实现此类的具体逻辑
 */
@Slf4j
// @Service
public class QwenAiServiceImpl implements AiService {

    @Override
    public String chat(String message, List<ChatMessage> history) {
        log.warn("通义千问AI服务尚未实现");
        return "通义千问AI服务尚未配置,请联系管理员";
    }

    @Override
    public List<DishRecommendation> recommendDishes(Long familyId, Long userId, String mealPeriod,
            String preferences, Integer count) {
        log.warn("通义千问AI服务尚未实现");
        return new ArrayList<>();
    }

    @Override
    public OrderParseResult parseOrderText(String text, Long familyId) {
        log.warn("通义千问AI服务尚未实现");
        OrderParseResult result = new OrderParseResult();
        result.setSuccess(false);
        result.setErrorMessage("通义千问AI服务尚未配置");
        return result;
    }

    @Override
    public String generateWeeklyMenu(Long familyId, String preferences) {
        log.warn("通义千问AI服务尚未实现");
        return "{\"error\": \"通义千问AI服务尚未配置\"}";
    }

    @Override
    public String generateDishDescription(String dishName) {
        log.warn("通义千问AI服务尚未实现");
        return "通义千问AI服务尚未配置,请联系管理员";
    }
}
