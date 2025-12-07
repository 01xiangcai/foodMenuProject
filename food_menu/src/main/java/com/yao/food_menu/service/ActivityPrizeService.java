package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.dto.PrizeConfigDto;
import com.yao.food_menu.entity.ActivityPrize;

import java.util.List;

/**
 * 活动奖品服务接口
 */
public interface ActivityPrizeService extends IService<ActivityPrize> {

    /**
     * 添加奖品
     */
    Long addPrize(Long activityId, PrizeConfigDto dto);

    /**
     * 更新奖品
     */
    void updatePrize(Long prizeId, PrizeConfigDto dto);

    /**
     * 删除奖品
     */
    void deletePrize(Long prizeId);

    /**
     * 获取活动奖品列表
     */
    List<PrizeConfigDto> getActivityPrizes(Long activityId);

    /**
     * 抽奖算法 - 根据概率抽取奖品
     */
    ActivityPrize drawPrize(Long activityId);

    /**
     * 扣减奖品库存
     */
    void decreaseStock(Long prizeId);
}
