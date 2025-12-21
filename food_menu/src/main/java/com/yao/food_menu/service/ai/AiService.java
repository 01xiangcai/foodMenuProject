package com.yao.food_menu.service.ai;

import com.yao.food_menu.dto.ai.ChatMessage;
import com.yao.food_menu.dto.ai.DishRecommendation;
import com.yao.food_menu.dto.ai.OrderParseResult;

import java.util.List;

/**
 * AI服务核心接口
 * 定义统一的AI能力,所有AI服务提供商都需要实现此接口
 * 这样可以方便地切换不同的AI服务商
 */
public interface AiService {

    /**
     * AI对话
     * 
     * @param message 用户消息
     * @param history 历史对话记录(可选)
     * @return AI回复
     */
    String chat(String message, List<ChatMessage> history);

    /**
     * 菜品推荐
     * 
     * @param familyId    家庭ID
     * @param userId      用户ID
     * @param mealPeriod  餐次(早餐/午餐/晚餐)
     * @param preferences 用户偏好描述
     * @param count       推荐数量
     * @return 推荐的菜品列表
     */
    List<DishRecommendation> recommendDishes(Long familyId, Long userId, String mealPeriod,
            String preferences, Integer count);

    /**
     * 智能解析下单文本
     * 例如: "我要两份宫保鸡丁和一份鱼香肉丝"
     * 
     * @param text     用户输入的文本
     * @param familyId 家庭ID(用于查询可用菜品)
     * @return 解析结果
     */
    OrderParseResult parseOrderText(String text, Long familyId);

    /**
     * 生成一周菜单
     * 
     * @param familyId    家庭ID
     * @param preferences 偏好设置(营养均衡、预算等)
     * @return 一周菜单的JSON字符串
     */
    String generateWeeklyMenu(Long familyId, String preferences);
}
