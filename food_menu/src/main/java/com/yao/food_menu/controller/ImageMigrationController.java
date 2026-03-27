package com.yao.food_menu.controller;

import com.yao.food_menu.common.Result;
import com.yao.food_menu.dto.MigrationStatus;
import com.yao.food_menu.service.ImageMigrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 图片迁移管理控制器
 * 仅供管理员使用
 */
@RestController
@RequestMapping("/admin/migration")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "图片迁移管理", description = "OSS图片迁移到本地存储")
public class ImageMigrationController {

    private final ImageMigrationService migrationService;
    private final com.yao.food_menu.service.ThumbnailService thumbnailService;

    @PostMapping("/thumbnails/batch")
    @Operation(summary = "为所有历史图片一键生成缩略图")
    public Result<String> batchGenerateThumbnails() {
        log.info("管理员触发批量生成缩略图任务");
        thumbnailService.batchGenerateThumbnails();
        return Result.success("任务已在后台启动，请查看进度");
    }

    @GetMapping("/thumbnails/status")
    @Operation(summary = "查询缩略图补全任务进度")
    public Result<MigrationStatus> getThumbnailStatus() {
        return Result.success(thumbnailService.getThumbnailStatus());
    }

    @GetMapping("/thumbnails/check")
    @Operation(summary = "检查缺失缩略图的数量")
    public Result<Integer> checkThumbnails() {
        log.info("管理员触发缩略图检查任务");
        int count = thumbnailService.countMissingThumbnails();
        return Result.success(count);
    }


    @PostMapping("/dishes")
    @Operation(summary = "迁移菜品和轮播图图片")
    public Result<String> migrateDishImages() {
        log.info("开始迁移菜品和轮播图图片");
        migrationService.migrateDishImages();
        return Result.success("迁移任务已启动,请查看状态");
    }

    @PostMapping("/avatars")
    @Operation(summary = "迁移用户头像")
    public Result<String> migrateAvatars() {
        log.info("开始迁移用户头像");
        migrationService.migrateAvatars();
        return Result.success("迁移任务已启动,请查看状态");
    }

    @GetMapping("/status/dishes")
    @Operation(summary = "查看菜品图片迁移状态")
    public Result<MigrationStatus> getDishMigrationStatus() {
        return Result.success(migrationService.getDishMigrationStatus());
    }

    @GetMapping("/status/avatars")
    @Operation(summary = "查看头像迁移状态")
    public Result<MigrationStatus> getAvatarMigrationStatus() {
        return Result.success(migrationService.getAvatarMigrationStatus());
    }

    @PostMapping("/stop")
    @Operation(summary = "停止迁移任务")
    public Result<String> stopMigration() {
        log.info("停止迁移任务");
        migrationService.stopMigration();
        return Result.success("迁移任务已停止");
    }
}
