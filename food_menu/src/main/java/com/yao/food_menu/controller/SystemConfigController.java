package com.yao.food_menu.controller;

import com.yao.food_menu.common.Result;
import com.yao.food_menu.entity.SystemConfig;
import com.yao.food_menu.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置管理 Controller
 */
@Tag(name = "系统配置管理", description = "系统配置的查询和更新")
@RestController
@RequestMapping("/systemConfig")
@Slf4j
public class SystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    /**
     * 根据配置键获取配置值
     */
    @Operation(summary = "获取配置值", description = "根据配置键获取配置值")
    @GetMapping("/{key}")
    public Result<Map<String, String>> getConfig(@PathVariable String key) {
        log.info("获取配置: key={}", key);
        String value = systemConfigService.getConfigValue(key);
        Map<String, String> result = new HashMap<>();
        result.put("configKey", key);
        result.put("configValue", value);
        return Result.success(result);
    }

    /**
     * 更新配置值
     */
    @Operation(summary = "更新配置值", description = "更新指定配置键的值")
    @PutMapping
    public Result<String> updateConfig(@RequestBody Map<String, String> params) {
        String configKey = params.get("configKey");
        String configValue = params.get("configValue");
        log.info("更新配置: key={}, value={}", configKey, configValue);
        
        if (configKey == null || configValue == null) {
            return Result.error("配置键和配置值不能为空");
        }
        
        boolean success = systemConfigService.updateConfigValue(configKey, configValue);
        if (success) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    /**
     * 获取所有配置
     */
    @Operation(summary = "获取所有配置", description = "获取所有系统配置")
    @GetMapping("/list")
    public Result<List<SystemConfig>> getAllConfigs() {
        log.info("获取所有配置");
        List<SystemConfig> configs = systemConfigService.list();
        return Result.success(configs);
    }
}

