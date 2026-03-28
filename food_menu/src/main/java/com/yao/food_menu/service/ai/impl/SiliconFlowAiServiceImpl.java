package com.yao.food_menu.service.ai.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yao.food_menu.config.AiConfig;
import com.yao.food_menu.dto.ai.ChatMessage;
import com.yao.food_menu.dto.ai.DishRecommendation;
import com.yao.food_menu.dto.ai.OrderParseItem;
import com.yao.food_menu.dto.ai.OrderParseResult;
import com.yao.food_menu.entity.Dish;
import com.yao.food_menu.service.DishService;
import com.yao.food_menu.service.ai.AiService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 硅基流动AI服务实现
 * 兼容OpenAI格式的API调用
 */
@Slf4j
// @Service
public class SiliconFlowAiServiceImpl implements AiService {

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    private DishService dishService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private OkHttpClient httpClient;

    /**
     * 初始化HTTP客户端
     */
    private OkHttpClient getHttpClient() {
        if (httpClient == null) {
            AiConfig.SiliconFlowConfig config = aiConfig.getSiliconFlow();
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
            AiConfig.SiliconFlowConfig config = aiConfig.getSiliconFlow();

            // 构建消息列表
            List<Map<String, String>> messages = new ArrayList<>();

            // 添加历史消息
            if (history != null && !history.isEmpty()) {
                for (ChatMessage msg : history) {
                    Map<String, String> msgMap = new HashMap<>();
                    msgMap.put("role", msg.getRole());
                    msgMap.put("content", msg.getContent());
                    messages.add(msgMap);
                }
            }

            // 添加当前消息
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", message);
            messages.add(userMsg);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModel());
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", config.getMaxTokens());
            requestBody.put("temperature", config.getTemperature());

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            // 发送请求
            Request request = new Request.Builder()
                    .url(config.getBaseUrl() + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + config.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .build();

            try (Response response = getHttpClient().newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("AI API调用失败: {}", response.code());
                    throw new IOException("AI API调用失败: " + response.code());
                }

                String responseBody = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(responseBody);

                // 解析响应
                String reply = jsonNode.path("choices").get(0)
                        .path("message").path("content").asText();

                log.info("AI回复: {}", reply);
                return reply;
            }

        } catch (Exception e) {
            log.error("AI对话失败", e);
            return "抱歉,AI服务暂时不可用,请稍后再试";
        }
    }

    @Override
    public List<DishRecommendation> recommendDishes(Long familyId, Long userId, String mealPeriod,
            String preferences, Integer count) {
        try {
            // 获取家庭的所有菜品
            List<Dish> dishes = dishService.list(
                    new LambdaQueryWrapper<Dish>()
                            .eq(Dish::getFamilyId, familyId)
                            .eq(Dish::getStatus, 1));

            if (dishes == null || dishes.isEmpty()) {
                log.warn("家庭{}没有可用菜品", familyId);
                return new ArrayList<>();
            }

            // 构建菜品列表字符串
            String dishList = dishes.stream()
                    .map(d -> d.getName() + "(ID:" + d.getId() + ")")
                    .collect(Collectors.joining(", "));

            // 构建提示词
            String prompt = String.format(
                    "你是一个专业的营养师和美食顾问。请从以下菜品中推荐%d道适合%s的菜品。\n\n" +
                            "可选菜品: %s\n\n" +
                            "用户偏好: %s\n\n" +
                            "请以JSON格式返回推荐结果,格式如下:\n" +
                            "[{\"dishId\": 1, \"dishName\": \"菜品名\", \"reason\": \"推荐理由\", \"score\": 85}]\n\n" +
                            "只返回JSON,不要其他说明文字。",
                    count, mealPeriod, dishList, preferences != null ? preferences : "营养均衡");

            String response = chat(prompt, null);

            // 解析JSON响应
            response = response.trim();
            if (response.startsWith("```json")) {
                response = response.substring(7);
            }
            if (response.endsWith("```")) {
                response = response.substring(0, response.length() - 3);
            }
            response = response.trim();

            JsonNode jsonNode = objectMapper.readTree(response);
            List<DishRecommendation> recommendations = new ArrayList<>();

            for (JsonNode node : jsonNode) {
                DishRecommendation rec = new DishRecommendation();
                rec.setDishId(node.path("dishId").asLong());
                rec.setDishName(node.path("dishName").asText());
                rec.setReason(node.path("reason").asText());
                rec.setScore(node.path("score").asInt());
                recommendations.add(rec);
            }

            return recommendations;

        } catch (Exception e) {
            log.error("菜品推荐失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public OrderParseResult parseOrderText(String text, Long familyId) {
        OrderParseResult result = new OrderParseResult();

        try {
            // 获取家庭的所有菜品
            List<Dish> dishes = dishService.list(
                    new LambdaQueryWrapper<Dish>()
                            .eq(Dish::getFamilyId, familyId)
                            .eq(Dish::getStatus, 1));

            if (dishes == null || dishes.isEmpty()) {
                result.setSuccess(false);
                result.setErrorMessage("没有可用菜品");
                return result;
            }

            // 构建菜品列表
            String dishList = dishes.stream()
                    .map(d -> d.getName() + "(ID:" + d.getId() + ")")
                    .collect(Collectors.joining(", "));

            // 构建提示词
            String prompt = String.format(
                    "你是一个智能点餐助手。请从用户的输入中识别菜品名称和数量。\n\n" +
                            "用户输入: %s\n\n" +
                            "可选菜品: %s\n\n" +
                            "请以JSON格式返回识别结果,格式如下:\n" +
                            "[{\"dishName\": \"宫保鸡丁\", \"quantity\": 2, \"dishId\": 1}]\n\n" +
                            "如果菜品名称不在可选列表中,dishId设为null。只返回JSON,不要其他说明文字。",
                    text, dishList);

            String response = chat(prompt, null);

            // 解析JSON响应
            response = response.trim();
            if (response.startsWith("```json")) {
                response = response.substring(7);
            }
            if (response.endsWith("```")) {
                response = response.substring(0, response.length() - 3);
            }
            response = response.trim();

            JsonNode jsonNode = objectMapper.readTree(response);
            List<OrderParseItem> items = new ArrayList<>();

            for (JsonNode node : jsonNode) {
                OrderParseItem item = new OrderParseItem();
                item.setDishName(node.path("dishName").asText());
                item.setQuantity(node.path("quantity").asInt());

                if (!node.path("dishId").isNull()) {
                    item.setDishId(node.path("dishId").asLong());
                }

                items.add(item);
            }

            result.setItems(items);
            result.setSuccess(true);

        } catch (Exception e) {
            log.error("订单解析失败", e);
            result.setSuccess(false);
            result.setErrorMessage("解析失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public String generateWeeklyMenu(Long familyId, String preferences) {
        try {
            // 获取家庭的所有菜品
            List<Dish> dishes = dishService.list(
                    new LambdaQueryWrapper<Dish>()
                            .eq(Dish::getFamilyId, familyId)
                            .eq(Dish::getStatus, 1));

            if (dishes == null || dishes.isEmpty()) {
                return "{\"error\": \"没有可用菜品\"}";
            }

            // 构建菜品列表
            String dishList = dishes.stream()
                    .map(Dish::getName)
                    .collect(Collectors.joining(", "));

            // 构建提示词
            String prompt = String.format(
                    "你是一个专业的营养师。请为一个家庭制定一周(7天)的菜单,每天包括早餐、午餐、晚餐。\n\n" +
                            "可选菜品: %s\n\n" +
                            "要求: %s\n\n" +
                            "请以JSON格式返回,格式如下:\n" +
                            "{\n" +
                            "  \"monday\": {\"breakfast\": [\"菜品1\"], \"lunch\": [\"菜品2\", \"菜品3\"], \"dinner\": [\"菜品4\"]},\n"
                            +
                            "  \"tuesday\": {...},\n" +
                            "  ...\n" +
                            "}\n\n" +
                            "注意:\n" +
                            "1. 尽量不重复\n" +
                            "2. 营养均衡\n" +
                            "3. 荤素搭配\n\n" +
                            "只返回JSON,不要其他说明文字。",
                    dishList, preferences != null ? preferences : "营养均衡、荤素搭配");

            String response = chat(prompt, null);

            // 清理响应
            response = response.trim();
            if (response.startsWith("```json")) {
                response = response.substring(7);
            }
            if (response.endsWith("```")) {
                response = response.substring(0, response.length() - 3);
            }

            return response.trim();

        } catch (Exception e) {
            log.error("生成一周菜单失败", e);
            return "{\"error\": \"生成失败\"}";
        }
    }
}
