package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.common.context.FamilyContext;
import com.yao.food_menu.common.exception.BusinessException;
import com.yao.food_menu.dto.ParticipateResultDto;
import com.yao.food_menu.dto.PrizeConfigDto;
import com.yao.food_menu.entity.ActivityParticipateRecord;
import com.yao.food_menu.entity.ActivityPrize;
import com.yao.food_menu.entity.MarketingActivity;
import com.yao.food_menu.mapper.ActivityParticipateRecordMapper;
import com.yao.food_menu.service.ActivityParticipateService;
import com.yao.food_menu.service.ActivityPrizeService;
import com.yao.food_menu.service.MarketingActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 活动参与服务实现类
 */
@Service
@RequiredArgsConstructor
public class ActivityParticipateServiceImpl
        extends ServiceImpl<ActivityParticipateRecordMapper, ActivityParticipateRecord>
        implements ActivityParticipateService {

    private final MarketingActivityService marketingActivityService;
    private final ActivityPrizeService activityPrizeService;

    @Override
    public boolean checkEligibility(Long activityId, Long userId) {
        MarketingActivity activity = marketingActivityService.getById(activityId);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }

        // 检查活动状态
        if (activity.getStatus() != 1) {
            throw new BusinessException("活动未开始或已结束");
        }

        // 检查活动时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime()) || now.isAfter(activity.getEndTime())) {
            throw new BusinessException("不在活动时间范围内");
        }

        // 检查参与次数
        Integer remainTimes = getRemainTimes(activityId, userId);
        if (remainTimes != null && remainTimes <= 0) {
            throw new BusinessException("参与次数已用完");
        }

        return true;
    }

    @Override
    public Integer getRemainTimes(Long activityId, Long userId) {
        MarketingActivity activity = marketingActivityService.getById(activityId);
        if (activity == null) {
            return 0;
        }

        // 如果不限制次数
        if (activity.getParticipateLimit() == null || activity.getParticipateLimit() == 0) {
            return null; // null表示不限次数
        }

        LambdaQueryWrapper<ActivityParticipateRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityParticipateRecord::getActivityId, activityId)
                .eq(ActivityParticipateRecord::getWxUserId, userId);

        // 根据限制类型查询
        if ("DAILY".equals(activity.getLimitType())) {
            // 每日限制
            LocalDate today = LocalDate.now();
            wrapper.ge(ActivityParticipateRecord::getParticipateTime, today.atStartOfDay())
                    .lt(ActivityParticipateRecord::getParticipateTime, today.plusDays(1).atStartOfDay());
        }
        // TOTAL类型不需要额外条件

        long count = this.count(wrapper);
        return (int) (activity.getParticipateLimit() - count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParticipateResultDto participate(Long activityId, Long userId) {
        // 检查参与资格
        checkEligibility(activityId, userId);

        // 抽奖
        ActivityPrize prize = activityPrizeService.drawPrize(activityId);

        // 创建参与记录
        ActivityParticipateRecord record = new ActivityParticipateRecord();
        record.setActivityId(activityId);
        record.setWxUserId(userId);
        record.setParticipateTime(LocalDateTime.now());
        record.setPrizeId(prize.getId());
        record.setFamilyId(FamilyContext.getCurrentFamilyId());

        // 判断是否中奖(非"谢谢参与"类型)
        boolean isWin = !"THANK_YOU".equals(prize.getPrizeType());
        record.setIsWin(isWin ? 1 : 0);
        record.setPrizeStatus(0); // 未领取

        // 设置过期时间(7天后)
        record.setExpireTime(LocalDateTime.now().plusDays(7));

        this.save(record);

        // 如果中奖,扣减库存
        if (isWin) {
            activityPrizeService.decreaseStock(prize.getId());
        }

        // 构建返回结果
        ParticipateResultDto result = new ParticipateResultDto();
        result.setIsWin(isWin);
        result.setRecordId(record.getId());
        result.setRemainTimes(getRemainTimes(activityId, userId));

        if (isWin) {
            PrizeConfigDto prizeDto = new PrizeConfigDto();
            BeanUtils.copyProperties(prize, prizeDto);
            result.setPrize(prizeDto);
            result.setMessage("恭喜您中奖了!");
        } else {
            result.setMessage("谢谢参与,再接再厉!");
        }

        return result;
    }

    @Override
    public void claimPrize(Long recordId) {
        ActivityParticipateRecord record = this.getById(recordId);
        if (record == null) {
            throw new BusinessException("参与记录不存在");
        }

        if (record.getIsWin() == 0) {
            throw new BusinessException("未中奖,无法领取");
        }

        if (record.getPrizeStatus() != 0) {
            throw new BusinessException("奖品已领取");
        }

        // 检查是否过期
        if (LocalDateTime.now().isAfter(record.getExpireTime())) {
            record.setPrizeStatus(3); // 已过期
            this.updateById(record);
            throw new BusinessException("奖品已过期");
        }

        record.setPrizeStatus(1); // 已领取
        record.setClaimTime(LocalDateTime.now());
        this.updateById(record);
    }
}
