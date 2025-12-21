package com.yao.food_menu.service;

import com.yao.food_menu.dto.ai.OrderParseResult;
import com.yao.food_menu.service.ai.AiService;
import com.yao.food_menu.service.ai.AiServiceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 智能下单服务
 * 解析用户的自然语言输入,识别菜品和数量
 */
@Slf4j
@Service
public class SmartOrderService {

    @Autowired
    private AiServiceFactory aiServiceFactory;

    /**
     * 解析下单文本
     * 例如: "我要两份宫保鸡丁和一份鱼香肉丝"
     * 
     * @param text     用户输入的文本
     * @param familyId 家庭ID
     * @return 解析结果
     */
    public OrderParseResult parseOrder(String text, Long familyId) {
        try {
            // 获取AI服务
            AiService aiService = aiServiceFactory.getAiService();

            // 调用AI解析
            OrderParseResult result = aiService.parseOrderText(text, familyId);

            log.info("解析订单文本: {}, 识别到{}个菜品", text, result.getItems().size());
            return result;

        } catch (Exception e) {
            log.error("解析订单失败", e);
            OrderParseResult result = new OrderParseResult();
            result.setSuccess(false);
            result.setErrorMessage("解析失败: " + e.getMessage());
            return result;
        }
    }
}
