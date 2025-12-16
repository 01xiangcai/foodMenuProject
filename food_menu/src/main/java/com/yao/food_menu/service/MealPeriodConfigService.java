package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.entity.MealPeriodConfig;
import com.yao.food_menu.enums.MealPeriod;

import java.util.List;

/**
 * 餐次配置服务
 */
public interface MealPeriodConfigService extends IService<MealPeriodConfig> {

    /**
     * 获取家庭的所有餐次配置
     */
    List<MealPeriodConfig> getByFamilyId(Long familyId);

    /**
     * 获取家庭某个餐次的配置
     */
    MealPeriodConfig getByFamilyAndPeriod(Long familyId, String mealPeriod);

    /**
     * 保存或更新餐次配置
     */
    void saveOrUpdateConfig(MealPeriodConfig config);

    /**
     * 根据当前时间获取餐次
     */
    MealPeriod getCurrentPeriod(Long familyId);

    /**
     * 批量保存餐次配置
     */
    void batchSaveConfigs(Long familyId, List<MealPeriodConfig> configs);
}
