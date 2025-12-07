package com.yao.food_menu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yao.food_menu.common.Result;
import com.yao.food_menu.dto.ActivityCreateDto;
import com.yao.food_menu.dto.ActivityDetailDto;
import com.yao.food_menu.dto.PrizeConfigDto;
import com.yao.food_menu.service.ActivityPrizeService;
import com.yao.food_menu.service.MarketingActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端营销活动控制器
 */
@Tag(name = "管理端-营销活动管理", description = "营销活动的增删改查")
@RestController
@RequestMapping("/admin/marketing/activity")
@Slf4j
@RequiredArgsConstructor
public class AdminMarketingActivityController {

    private final MarketingActivityService marketingActivityService;
    private final ActivityPrizeService activityPrizeService;

    @Operation(summary = "创建活动", description = "创建新的营销活动")
    @PostMapping
    public Result<Long> createActivity(@RequestBody ActivityCreateDto dto) {
        log.info("创建活动: {}", dto);
        Long activityId = marketingActivityService.createActivity(dto);
        return Result.success(activityId);
    }

    @Operation(summary = "更新活动", description = "更新营销活动信息")
    @PutMapping("/{id}")
    public Result<String> updateActivity(@PathVariable Long id, @RequestBody ActivityCreateDto dto) {
        log.info("更新活动: id={}, dto={}", id, dto);
        marketingActivityService.updateActivity(id, dto);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除活动", description = "删除营销活动")
    @DeleteMapping("/{id}")
    public Result<String> deleteActivity(@PathVariable Long id) {
        log.info("删除活动: id={}", id);
        marketingActivityService.deleteActivity(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取活动详情", description = "根据ID获取活动详情")
    @GetMapping("/{id}")
    public Result<ActivityDetailDto> getActivityDetail(@PathVariable Long id) {
        ActivityDetailDto detail = marketingActivityService.getActivityDetail(id);
        return Result.success(detail);
    }

    @Operation(summary = "分页查询活动列表", description = "分页查询营销活动列表")
    @GetMapping("/page")
    public Result<Page<ActivityDetailDto>> getActivityPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String activityType,
            @RequestParam(required = false) Integer status) {
        log.info("分页查询活动: pageNum={}, pageSize={}, activityType={}, status={}",
                pageNum, pageSize, activityType, status);
        Page<ActivityDetailDto> page = marketingActivityService.getActivityPage(pageNum, pageSize, activityType,
                status);
        return Result.success(page);
    }

    @Operation(summary = "更新活动状态", description = "启动/暂停/结束活动")
    @PutMapping("/{id}/status")
    public Result<String> updateActivityStatus(@PathVariable Long id, @RequestParam Integer status) {
        log.info("更新活动状态: id={}, status={}", id, status);
        marketingActivityService.updateActivityStatus(id, status);
        return Result.success("状态更新成功");
    }

    @Operation(summary = "添加奖品", description = "为活动添加奖品")
    @PostMapping("/{activityId}/prize")
    public Result<Long> addPrize(@PathVariable Long activityId, @RequestBody PrizeConfigDto dto) {
        log.info("添加奖品: activityId={}, dto={}", activityId, dto);
        Long prizeId = activityPrizeService.addPrize(activityId, dto);
        return Result.success(prizeId);
    }

    @Operation(summary = "更新奖品", description = "更新奖品信息")
    @PutMapping("/prize/{prizeId}")
    public Result<String> updatePrize(@PathVariable Long prizeId, @RequestBody PrizeConfigDto dto) {
        log.info("更新奖品: prizeId={}, dto={}", prizeId, dto);
        activityPrizeService.updatePrize(prizeId, dto);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除奖品", description = "删除活动奖品")
    @DeleteMapping("/prize/{prizeId}")
    public Result<String> deletePrize(@PathVariable Long prizeId) {
        log.info("删除奖品: prizeId={}", prizeId);
        activityPrizeService.deletePrize(prizeId);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取活动奖品列表", description = "获取指定活动的所有奖品")
    @GetMapping("/{activityId}/prizes")
    public Result<List<PrizeConfigDto>> getActivityPrizes(@PathVariable Long activityId) {
        List<PrizeConfigDto> prizes = activityPrizeService.getActivityPrizes(activityId);
        return Result.success(prizes);
    }
}
