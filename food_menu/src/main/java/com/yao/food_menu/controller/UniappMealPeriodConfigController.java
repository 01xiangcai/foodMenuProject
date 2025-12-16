package com.yao.food_menu.controller;

import com.yao.food_menu.common.Result;
import com.yao.food_menu.common.util.JwtUtil;
import com.yao.food_menu.entity.MealPeriodConfig;
import com.yao.food_menu.entity.WxUser;
import com.yao.food_menu.enums.MealPeriod;
import com.yao.food_menu.service.MealPeriodConfigService;
import com.yao.food_menu.service.WxUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序餐次配置Controller
 */
@Tag(name = "小程序餐次配置", description = "餐次时段配置管理")
@RestController
@RequestMapping("/uniapp/meal-config")
@Slf4j
public class UniappMealPeriodConfigController {

    @Autowired
    private MealPeriodConfigService mealPeriodConfigService;

    @Autowired
    private WxUserService wxUserService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取当前家庭的餐次配置
     */
    @Operation(summary = "获取餐次配置", description = "获取当前家庭的餐次配置")
    @GetMapping
    public Result<List<MealPeriodConfig>> getConfig(@RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = jwtUtil.getUserId(token);
            WxUser wxUser = wxUserService.getById(userId);
            if (wxUser == null || wxUser.getFamilyId() == null) {
                return Result.error("用户信息不完整");
            }

            List<MealPeriodConfig> configs = mealPeriodConfigService.getByFamilyId(wxUser.getFamilyId());
            return Result.success(configs);
        } catch (Exception e) {
            log.error("获取餐次配置失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 保存餐次配置(仅管理员)
     */
    @Operation(summary = "保存餐次配置", description = "保存餐次配置,仅管理员可操作")
    @PostMapping
    public Result<String> saveConfig(@RequestBody List<MealPeriodConfig> configs,
            @RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = jwtUtil.getUserId(token);
            WxUser wxUser = wxUserService.getById(userId);
            if (wxUser == null || wxUser.getFamilyId() == null) {
                return Result.error("用户信息不完整");
            }

            // 检查是否为管理员
            if (!wxUserService.isAdmin(userId)) {
                return Result.error("无权限操作");
            }

            mealPeriodConfigService.batchSaveConfigs(wxUser.getFamilyId(), configs);
            return Result.success("保存成功");
        } catch (Exception e) {
            log.error("保存餐次配置失败", e);
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前餐次
     */
    @Operation(summary = "获取当前餐次", description = "根据当前时间和配置判断当前餐次")
    @GetMapping("/current-period")
    public Result<String> getCurrentPeriod(@RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = jwtUtil.getUserId(token);
            WxUser wxUser = wxUserService.getById(userId);
            if (wxUser == null || wxUser.getFamilyId() == null) {
                return Result.error("用户信息不完整");
            }

            MealPeriod period = mealPeriodConfigService.getCurrentPeriod(wxUser.getFamilyId());
            return Result.success(period.getCode());
        } catch (Exception e) {
            log.error("获取当前餐次失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }
}
