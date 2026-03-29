package com.yao.food_menu.controller;

import com.yao.food_menu.common.Result;
import com.yao.food_menu.dto.AiChatRequest;
import com.yao.food_menu.dto.AiChatResponse;
import com.yao.food_menu.dto.DishRecommendRequest;
import com.yao.food_menu.dto.SmartOrderRequest;
import com.yao.food_menu.dto.ai.DishRecommendation;
import com.yao.food_menu.dto.ai.OrderParseResult;
import com.yao.food_menu.entity.WxUser;
import com.yao.food_menu.service.AiChatService;
import com.yao.food_menu.service.MenuRecommendService;
import com.yao.food_menu.service.SmartOrderService;
import com.yao.food_menu.service.WxUserService;
import com.yao.food_menu.service.SystemConfigService;
import com.yao.food_menu.config.AiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序端AI助手控制器
 */
@Slf4j
@RestController
@RequestMapping("/wx/ai")
@Tag(name = "小程序端AI助手接口")
public class WxAiAssistantController {

    @Autowired
    private AiChatService aiChatService;

    @Autowired
    private MenuRecommendService menuRecommendService;

    @Autowired
    private SmartOrderService smartOrderService;

    @Autowired
    private WxUserService wxUserService;

    @Autowired
    private com.yao.food_menu.service.ai.AiServiceFactory aiServiceFactory;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private AiConfig aiConfig;

    /**
     * AI对话
     */
    @PostMapping("/chat")
    @Operation(summary = "AI对话", description = "与AI助手进行对话")
    public Result<AiChatResponse> chat(@RequestHeader("wx-user-id") Long wxUserId,
            @RequestBody AiChatRequest request) {
        try {
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return Result.error("消息内容不能为空");
            }

            // 调用AI对话服务
            String reply = aiChatService.chat(
                    wxUserId,
                    request.getMessage(),
                    request.getIncludeHistory() != null && request.getIncludeHistory());

            AiChatResponse response = new AiChatResponse(reply, null);
            return Result.success(response);

        } catch (Exception e) {
            log.error("AI对话失败", e);
            return Result.error("AI对话失败: " + e.getMessage());
        }
    }

    /**
     * 获取AI推荐菜品
     */
    @PostMapping("/recommend")
    @Operation(summary = "获取AI推荐菜品", description = "基于用户偏好推荐菜品")
    public Result<List<DishRecommendation>> recommend(@RequestHeader("wx-user-id") Long wxUserId,
            @RequestBody DishRecommendRequest request) {
        try {
            // 获取用户信息
            WxUser wxUser = wxUserService.getById(wxUserId);
            if (wxUser == null) {
                return Result.error("用户不存在");
            }

            // 调用推荐服务
            List<DishRecommendation> recommendations = menuRecommendService.getRecommendations(
                    wxUser.getFamilyId(),
                    wxUserId,
                    request.getMealPeriod(),
                    request.getPreferences(),
                    request.getCount());

            return Result.success(recommendations);

        } catch (Exception e) {
            log.error("菜品推荐失败", e);
            return Result.error("菜品推荐失败: " + e.getMessage());
        }
    }

    /**
     * 快速推荐 - 今天吃什么
     */
    @GetMapping("/quick-recommend")
    @Operation(summary = "快速推荐", description = "快速推荐今天吃什么")
    public Result<List<DishRecommendation>> quickRecommend(@RequestHeader("wx-user-id") Long wxUserId) {
        try {
            // 获取用户信息
            WxUser wxUser = wxUserService.getById(wxUserId);
            if (wxUser == null) {
                return Result.error("用户不存在");
            }

            // 调用快速推荐
            List<DishRecommendation> recommendations = menuRecommendService.quickRecommend(
                    wxUser.getFamilyId(),
                    wxUserId);

            return Result.success(recommendations);

        } catch (Exception e) {
            log.error("快速推荐失败", e);
            return Result.error("快速推荐失败: " + e.getMessage());
        }
    }

    /**
     * 智能解析下单文本
     */
    @PostMapping("/parse-order")
    @Operation(summary = "智能解析下单文本", description = "解析用户输入的自然语言下单")
    public Result<OrderParseResult> parseOrder(@RequestHeader("wx-user-id") Long wxUserId,
            @RequestBody SmartOrderRequest request) {
        try {
            if (request.getText() == null || request.getText().trim().isEmpty()) {
                return Result.error("输入文本不能为空");
            }

            // 获取用户信息
            WxUser wxUser = wxUserService.getById(wxUserId);
            if (wxUser == null) {
                return Result.error("用户不存在");
            }

            // 调用智能下单服务
            OrderParseResult result = smartOrderService.parseOrder(
                    request.getText(),
                    wxUser.getFamilyId());

            return Result.success(result);

        } catch (Exception e) {
            log.error("智能下单解析失败", e);
            return Result.error("智能下单解析失败: " + e.getMessage());
        }
    }

    /**
     * 清除对话历史
     */
    @DeleteMapping("/clear-history")
    @Operation(summary = "清除对话历史", description = "清除用户的AI对话历史")
    public Result<String> clearHistory(@RequestHeader("wx-user-id") Long wxUserId) {
        try {
            boolean success = aiChatService.clearHistory(wxUserId);

            if (success) {
                return Result.success("对话历史已清除");
            } else {
                return Result.error("清除对话历史失败");
            }

        } catch (Exception e) {
            log.error("清除对话历史失败", e);
            return Result.error("清除对话历史失败: " + e.getMessage());
        }
    }

    /**
     * 生成菜品简介
     */
    @PostMapping("/generate-dish-description")
    @Operation(summary = "生成菜品简介", description = "根据菜品名称AI自动生成简介")
    public Result<String> generateDishDescription(@RequestBody java.util.Map<String, String> params) {
        try {
            String dishName = params.get("dishName");
            if (dishName == null || dishName.trim().isEmpty()) {
                return Result.error("菜品名称不能为空");
            }

            com.yao.food_menu.service.ai.AiService aiService = aiServiceFactory.getAiService();
            String description = aiService.generateDishDescription(dishName);

            return Result.success(description);

        } catch (Exception e) {
            log.error("生成菜品简介失败", e);
            return Result.error("生成菜品简介失败: " + e.getMessage());
        }
    }

    /**
     * 获取 AI 客服动态 URL
     */
    @GetMapping("/customer-service-url")
    @Operation(summary = "获取AI客服URL", description = "获取包含动态AppKey的AI客服H5页面地址")
    public Result<String> getCustomerServiceUrl() {
        try {
            AiConfig.ExternalConfig config = aiConfig.getExternal();
            if (config == null) {
                return Result.error("未配置外部AI服务信息");
            }

            // 获取 AppKey (优先从数据库获取, 回退到 application.yml)
            String appKeyFromDb = systemConfigService.getConfigValue("ai_external_app_key");
            String appKey;
            if (appKeyFromDb != null && !appKeyFromDb.trim().isEmpty()) {
                appKey = appKeyFromDb;
                log.info("小程序AI客服: 使用数据库配置的 AppKey: {}", appKey);
            } else {
                appKey = config.getAppKey();
                log.info("小程序AI客服: 数据库未配置 AppKey, 使用配置文件默认值: {}", appKey);
            }

            if (appKey == null || appKey.trim().isEmpty()) {
                return Result.error("AI 外部服务 AppKey 未配置");
            }

            // 拼接动态 URL (格式与原硬编码一致)
            String dynamicUrl = config.getBaseUrl() + "/widget/h5-mobile.html?appKey=" + appKey;
            log.info("生成动态AI客服URL: {}", dynamicUrl);

            return Result.success(dynamicUrl);
        } catch (Exception e) {
            log.error("获取AI客服URL失败", e);
            return Result.error("获取AI客服URL失败: " + e.getMessage());
        }
    }
}
