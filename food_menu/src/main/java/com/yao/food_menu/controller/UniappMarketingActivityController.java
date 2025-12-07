package com.yao.food_menu.controller;

import com.yao.food_menu.common.Result;
import com.yao.food_menu.common.context.FamilyContext;
import com.yao.food_menu.dto.ActivityDetailDto;
import com.yao.food_menu.dto.ParticipateResultDto;
import com.yao.food_menu.dto.UserCouponDto;
import com.yao.food_menu.service.ActivityParticipateService;
import com.yao.food_menu.service.MarketingActivityService;
import com.yao.food_menu.service.UserCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序端营销活动控制器
 */
@Tag(name = "小程序端-营销活动", description = "小程序端营销活动相关接口")
@RestController
@RequestMapping("/uniapp/marketing/activity")
@Slf4j
@RequiredArgsConstructor
public class UniappMarketingActivityController {

    private final MarketingActivityService marketingActivityService;
    private final ActivityParticipateService activityParticipateService;
    private final UserCouponService userCouponService;

    @Operation(summary = "获取进行中的活动列表", description = "获取当前可参与的活动列表")
    @GetMapping("/list")
    public Result<List<ActivityDetailDto>> getActiveActivities() {
        List<ActivityDetailDto> activities = marketingActivityService.getActiveActivities();
        return Result.success(activities);
    }

    @Operation(summary = "获取活动详情", description = "获取活动详细信息")
    @GetMapping("/{id}")
    public Result<ActivityDetailDto> getActivityDetail(@PathVariable Long id) {
        ActivityDetailDto detail = marketingActivityService.getActivityDetail(id);
        return Result.success(detail);
    }

    @Operation(summary = "参与活动", description = "用户参与营销活动(抽奖/大转盘等)")
    @PostMapping("/{activityId}/participate")
    public Result<ParticipateResultDto> participate(@PathVariable Long activityId) {
        Long userId = FamilyContext.getUserId();
        log.info("用户参与活动: userId={}, activityId={}", userId, activityId);

        // 检查用户ID是否为空
        if (userId == null) {
            log.error("用户ID为空,无法参与活动");
            return Result.error("请先登录");
        }

        ParticipateResultDto result = activityParticipateService.participate(activityId, userId);
        return Result.success(result);
    }

    @Operation(summary = "获取剩余参与次数", description = "获取用户剩余参与次数")
    @GetMapping("/{activityId}/remain-times")
    public Result<Integer> getRemainTimes(@PathVariable Long activityId) {
        Long userId = FamilyContext.getUserId();
        Integer remainTimes = activityParticipateService.getRemainTimes(activityId, userId);
        return Result.success(remainTimes);
    }

    @Operation(summary = "领取奖品", description = "领取中奖奖品")
    @PostMapping("/record/{recordId}/claim")
    public Result<String> claimPrize(@PathVariable Long recordId) {
        log.info("领取奖品: recordId={}", recordId);
        activityParticipateService.claimPrize(recordId);
        return Result.success("领取成功");
    }

    @Operation(summary = "我的优惠券", description = "获取用户的优惠券列表")
    @GetMapping("/my-coupons")
    public Result<List<UserCouponDto>> getMyCoupons(@RequestParam(required = false) Integer status) {
        Long userId = FamilyContext.getUserId();
        List<UserCouponDto> coupons = userCouponService.getUserCoupons(userId, status);
        return Result.success(coupons);
    }
}
