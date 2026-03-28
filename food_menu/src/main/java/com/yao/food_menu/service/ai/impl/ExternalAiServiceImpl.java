package com.yao.food_menu.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yao.food_menu.config.AiConfig;
import com.yao.food_menu.dto.ai.ChatMessage;
import com.yao.food_menu.dto.ai.DishRecommendation;
import com.yao.food_menu.dto.ai.OrderParseResult;
import com.yao.food_menu.service.ai.AiService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 外部AI服务实现
 * 调用 aiCustomerService 项目提供的接口
 */
@Slf4j
@Service
public class ExternalAiServiceImpl implements AiService {

    @Autowired
    private AiConfig aiConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private OkHttpClient httpClient;

    /**
     * 初始化HTTP客户端
     */
    private OkHttpClient getHttpClient() {
        if (httpClient == null) {
            AiConfig.ExternalConfig config = aiConfig.getExternal();
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
                    .readTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
                    .writeTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
                    .build();
        }
        return httpClient;
    }

    @Override
    public String chat(String message, List<ChatMessage> history) {
        try {
            AiConfig.ExternalConfig config = aiConfig.getExternal();

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("message", message);
            requestBody.put("sessionId", "food-menu-session"); // 简单处理，实际可根据用户ID区分
            requestBody.put("userId", "food-menu-user");

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            // 发送请求 (调用 aiCustomerService 的公开对话接口)
            String url = config.getBaseUrl() + "/open/chat/" + config.getAppKey() + "/message";
            
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .build();

            try (Response response = getHttpClient().newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("外部AI API调用失败: {}", response.code());
                    return "抱歉,外部AI服务暂时不可用 (Code: " + response.code() + ")";
                }

                String responseBody = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(responseBody);

                // 根据 aiCustomerService 的 R.ok(reply) 结构解析
                // 通常结果在 data 字段中
                if (jsonNode.has("data")) {
                    return jsonNode.get("data").asText();
                } else {
                    return jsonNode.asText();
                }
            }

        } catch (Exception e) {
            log.error("外部AI对话失败", e);
            return "抱歉,处理您的请求时出错: " + e.getMessage();
        }
    }

    @Override
    public List<DishRecommendation> recommendDishes(Long familyId, Long userId, String mealPeriod,
            String preferences, Integer count) {
        // 由于推荐逻辑较为复杂且重度耦合旧版 AI 提示词，
        // 外部服务可能暂不支持特定格式的 JSON 返回。
        // 这里采用 Chat 接口包装 Prompt 来模拟。
        String prompt = String.format("请为家庭(ID:%d)推荐适合%s的%d道菜。偏好: %s。请返回JSON格式列表。", 
                familyId, mealPeriod, count, preferences);
        String reply = chat(prompt, null);
        log.info("外部AI推荐逻辑(暂未完全适配特定JSON结构): {}", reply);
        // TODO: 解析返回的JSON或适配外部服务专用推荐接口
        return new ArrayList<>();
    }

    @Override
    public OrderParseResult parseOrderText(String text, Long familyId) {
        // 逻辑类似 recommendDishes，需要适配外部服务的解析能力
        log.info("外部AI订单解析暂未适配: {}", text);
        OrderParseResult result = new OrderParseResult();
        result.setSuccess(false);
        result.setErrorMessage("外部服务暂不支持订单解析");
        return result;
    }

    @Override
    public String generateWeeklyMenu(Long familyId, String preferences) {
        String prompt = "请为家庭生成一周菜单。要求: " + preferences;
        return chat(prompt, null);
    }

    @Override
    public String generateDishDescription(String dishName) {
        String prompt = String.format(
                "你是一个专业的厨师和美食博主。请为菜品“%s”写一段诱人的简介。\n" +
                "要求：\n" +
                "1. 字数在 50-80 字左右。\n" +
                "2. 描述口感、香气和特色。\n" +
                "3. 语气生动，吸引食客。\n" +
                "只返回简介内容，不要其他说明文字。", dishName);
        
        return chat(prompt, null);
    }
}
