package com.yao.food_menu.controller;

import com.yao.food_menu.common.Result;
import com.yao.food_menu.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 公开的 AI 配置 Controller（免登录访问）
 */
@Tag(name = "公开配置", description = "供管理端 index.html 等加载的公开配置")
@RestController
@RequestMapping("/public/ai")
@Slf4j
public class PublicAiConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    /**
     * 获取 AI 客服所需的公开配置
     */
    @Operation(summary = "获取公开AI配置", description = "获取 AI 客服 AppKey 等信息")
    @GetMapping("/config")
    public Result<Map<String, String>> getPublicConfig() {
        String appKey = systemConfigService.getConfigValue("ai_external_app_key");
        String baseUrl = systemConfigService.getConfigValue("ai_external_base_url");
        
        Map<String, String> result = new HashMap<>();
        result.put("appKey", appKey);
        result.put("baseUrl", baseUrl);
        return Result.success(result);
    }
}
