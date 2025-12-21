package com.yao.food_menu.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yao.food_menu.common.Result;
import com.yao.food_menu.entity.User;
import com.yao.food_menu.service.UserService;
import com.yao.food_menu.service.ai.AiService;
import com.yao.food_menu.service.ai.AiServiceFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端AI菜单生成控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/ai")
@Tag(name = "管理端AI菜单接口")
public class AdminAiMenuController {

    @Autowired
    private AiServiceFactory aiServiceFactory;

    @Autowired
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 生成一周菜单请求DTO
     */
    @Data
    public static class WeeklyMenuRequest {
        private String preferences;
    }

    /**
     * 生成一周菜单
     */
    @PostMapping("/generate-weekly-menu")
    @Operation(summary = "生成一周菜单", description = "AI自动生成一周的菜单规划")
    public Result<JsonNode> generateWeeklyMenu(@RequestHeader("user-id") Long userId,
            @RequestBody WeeklyMenuRequest request) {
        try {
            // 获取用户信息
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 获取AI服务
            AiService aiService = aiServiceFactory.getAiService();

            // 生成一周菜单
            String menuJson = aiService.generateWeeklyMenu(
                    user.getFamilyId(),
                    request.getPreferences());

            // 解析JSON
            JsonNode jsonNode = objectMapper.readTree(menuJson);

            return Result.success(jsonNode);

        } catch (Exception e) {
            log.error("生成一周菜单失败", e);
            return Result.error("生成一周菜单失败: " + e.getMessage());
        }
    }

    /**
     * 分析用户偏好
     */
    @GetMapping("/analyze-preferences")
    @Operation(summary = "分析用户偏好", description = "分析家庭用户的饮食偏好")
    public Result<String> analyzePreferences(@RequestHeader("user-id") Long userId) {
        try {
            // TODO: 实现用户偏好分析逻辑
            // 可以基于历史订单、收藏、评论等数据

            return Result.success("用户偏好分析功能开发中");

        } catch (Exception e) {
            log.error("分析用户偏好失败", e);
            return Result.error("分析用户偏好失败: " + e.getMessage());
        }
    }
}
