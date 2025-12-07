package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.dto.ParticipateResultDto;
import com.yao.food_menu.entity.ActivityParticipateRecord;
import com.yao.food_menu.entity.ActivityPrize;

import java.util.List;

/**
 * 活动参与服务接口
 */
public interface ActivityParticipateService extends IService<ActivityParticipateRecord> {

    /**
     * 检查用户参与资格
     */
    boolean checkEligibility(Long activityId, Long userId);

    /**
     * 获取用户剩余参与次数
     */
    Integer getRemainTimes(Long activityId, Long userId);

    /**
     * 参与活动
     */
    ParticipateResultDto participate(Long activityId, Long userId);

    /**
     * 领取奖品
     */
    void claimPrize(Long recordId);

    /**
     * 获取我的奖品列表
     */
    List<ActivityPrize> getMyPrizes(Long userId);

    /**
     * 获取我的中奖记录
     */
    List<ActivityParticipateRecord> getMyRecords(Long userId);
}
