package com.yao.food_menu.service;

import com.yao.food_menu.dto.ai.DishRecommendation;
import com.yao.food_menu.service.ai.AiService;
import com.yao.food_menu.service.ai.AiServiceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜品推荐服务
 * 基于AI提供个性化菜品推荐
 */
@Slf4j
@Service
public class MenuRecommendService {

    @Autowired
    private AiServiceFactory aiServiceFactory;

    /**
     * 获取AI推荐的菜品
     * 
     * @param familyId    家庭ID
     * @param userId      用户ID
     * @param mealPeriod  餐次(早餐/午餐/晚餐)
     * @param preferences 用户偏好
     * @param count       推荐数量
     * @return 推荐的菜品列表
     */
    public List<DishRecommendation> getRecommendations(Long familyId, Long userId, String mealPeriod,
            String preferences, Integer count) {
        try {
            // 获取AI服务
            AiService aiService = aiServiceFactory.getAiService();

            // 调用AI推荐
            List<DishRecommendation> recommendations = aiService.recommendDishes(
                    familyId, userId, mealPeriod, preferences, count);

            log.info("为用户{}推荐了{}道菜品", userId, recommendations.size());
            return recommendations;

        } catch (Exception e) {
            log.error("菜品推荐失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 快速推荐 - 今天吃什么
     * 
     * @param familyId 家庭ID
     * @param userId   用户ID
     * @return 推荐的菜品列表
     */
    public List<DishRecommendation> quickRecommend(Long familyId, Long userId) {
        // 根据当前时间判断餐次
        int hour = java.time.LocalTime.now().getHour();
        String mealPeriod;

        if (hour < 10) {
            mealPeriod = "早餐";
        } else if (hour < 14) {
            mealPeriod = "午餐";
        } else {
            mealPeriod = "晚餐";
        }

        return getRecommendations(familyId, userId, mealPeriod, "营养均衡", 3);
    }
}
