package com.yao.food_menu.controller;

import com.yao.food_menu.common.Result;
import com.yao.food_menu.common.util.JwtUtil;
import com.yao.food_menu.entity.MealPeriodConfig;
import com.yao.food_menu.entity.User;
import com.yao.food_menu.service.MealPeriodConfigService;
import com.yao.food_menu.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin端餐次配置Controller
 */
@Tag(name = "Admin餐次配置", description = "Admin端餐次配置管理")
@RestController
@RequestMapping("/admin/meal-config")
@Slf4j
public class AdminMealPeriodConfigController {

    @Autowired
    private MealPeriodConfigService mealPeriodConfigService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取餐次配置
     */
    @Operation(summary = "获取餐次配置", description = "获取当前家庭或全局的餐次配置")
    @GetMapping
    public Result<List<MealPeriodConfig>> getConfig(@RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = jwtUtil.getUserId(token);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户信息不存在");
            }

            // 超级管理员可以查看所有家庭,其他管理员只能查看自己家庭
            Long familyId = user.getRole() == 2 ? null : user.getFamilyId();

            List<MealPeriodConfig> configs = familyId == null ? mealPeriodConfigService.list()
                    : mealPeriodConfigService.getByFamilyId(familyId);

            return Result.success(configs);
        } catch (Exception e) {
            log.error("获取餐次配置失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 保存餐次配置
     */
    @Operation(summary = "保存餐次配置", description = "保存餐次配置")
    @PostMapping
    public Result<String> saveConfig(@RequestBody List<MealPeriodConfig> configs,
            @RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = jwtUtil.getUserId(token);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户信息不存在");
            }

            // 检查权限:至少是家庭管理员
            if (user.getRole() == 0) {
                return Result.error("无权限操作");
            }

            // 设置familyId
            Long familyId = user.getFamilyId();
            configs.forEach(config -> {
                if (config.getFamilyId() == null) {
                    config.setFamilyId(familyId);
                }
            });

            mealPeriodConfigService.batchSaveConfigs(familyId, configs);
            return Result.success("保存成功");
        } catch (Exception e) {
            log.error("保存餐次配置失败", e);
            return Result.error("保存失败: " + e.getMessage());
        }
    }
}
