package com.yao.food_menu.controller;

import com.yao.food_menu.common.Result;
import com.yao.food_menu.dto.NotificationTypeConfigDTO;
import com.yao.food_menu.service.NotificationTypeConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端-通知类型配置控制器
 */
@Tag(name = "管理端-通知类型管理", description = "通知类型的增删改查")
@RestController
@RequestMapping("/admin/notification-type")
@Slf4j
@RequiredArgsConstructor
public class AdminNotificationTypeController {

    private final NotificationTypeConfigService notificationTypeConfigService;

    @Operation(summary = "获取所有通知类型", description = "获取所有通知类型配置列表")
    @GetMapping("/list")
    public Result<List<NotificationTypeConfigDTO>> getAllConfigs() {
        List<NotificationTypeConfigDTO> configs = notificationTypeConfigService.getAllConfigs();
        return Result.success(configs);
    }

    @Operation(summary = "新增通知类型", description = "新增自定义通知类型")
    @PostMapping
    public Result<Long> addConfig(@RequestBody NotificationTypeConfigDTO dto) {
        log.info("新增通知类型: code={}, name={}", dto.getCode(), dto.getName());
        Long id = notificationTypeConfigService.addConfig(dto);
        return Result.success(id);
    }

    @Operation(summary = "更新通知类型", description = "更新通知类型配置")
    @PutMapping("/{id}")
    public Result<String> updateConfig(@PathVariable Long id, @RequestBody NotificationTypeConfigDTO dto) {
        log.info("更新通知类型: id={}", id);
        notificationTypeConfigService.updateConfig(id, dto);
        return Result.success("更新成功");
    }

    @Operation(summary = "切换启用状态", description = "启用或禁用通知类型")
    @PutMapping("/{id}/toggle")
    public Result<String> toggleEnabled(@PathVariable Long id) {
        log.info("切换通知类型状态: id={}", id);
        notificationTypeConfigService.toggleEnabled(id);
        return Result.success("操作成功");
    }

    @Operation(summary = "删除通知类型", description = "删除自定义通知类型（系统预置不可删除）")
    @DeleteMapping("/{id}")
    public Result<String> deleteConfig(@PathVariable Long id) {
        log.info("删除通知类型: id={}", id);
        notificationTypeConfigService.deleteConfig(id);
        return Result.success("删除成功");
    }
}
