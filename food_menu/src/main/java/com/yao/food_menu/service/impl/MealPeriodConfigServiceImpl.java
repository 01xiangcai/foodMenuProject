package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.entity.MealPeriodConfig;
import com.yao.food_menu.enums.MealPeriod;
import com.yao.food_menu.mapper.MealPeriodConfigMapper;
import com.yao.food_menu.service.MealPeriodConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

/**
 * 餐次配置服务实现
 */
@Service
public class MealPeriodConfigServiceImpl extends ServiceImpl<MealPeriodConfigMapper, MealPeriodConfig>
        implements MealPeriodConfigService {

    @Override
    public List<MealPeriodConfig> getByFamilyId(Long familyId) {
        LambdaQueryWrapper<MealPeriodConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MealPeriodConfig::getFamilyId, familyId);
        wrapper.orderByAsc(MealPeriodConfig::getStartTime);
        return list(wrapper);
    }

    @Override
    public MealPeriodConfig getByFamilyAndPeriod(Long familyId, String mealPeriod) {
        LambdaQueryWrapper<MealPeriodConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MealPeriodConfig::getFamilyId, familyId);
        wrapper.eq(MealPeriodConfig::getMealPeriod, mealPeriod);
        return getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateConfig(MealPeriodConfig config) {
        MealPeriodConfig existing = getByFamilyAndPeriod(config.getFamilyId(), config.getMealPeriod());
        if (existing != null) {
            config.setId(existing.getId());
        }
        saveOrUpdate(config);
    }

    @Override
    public MealPeriod getCurrentPeriod(Long familyId) {
        List<MealPeriodConfig> configs = getByFamilyId(familyId);
        if (configs.isEmpty()) {
            // 如果没有配置,使用默认时间
            return MealPeriod.getCurrentPeriod(
                    LocalTime.now(),
                    LocalTime.of(6, 0),
                    LocalTime.of(10, 0),
                    LocalTime.of(14, 0));
        }

        // 获取早中晚餐的开始时间
        LocalTime breakfastStart = LocalTime.of(6, 0);
        LocalTime lunchStart = LocalTime.of(10, 0);
        LocalTime dinnerStart = LocalTime.of(14, 0);

        for (MealPeriodConfig config : configs) {
            if ("BREAKFAST".equals(config.getMealPeriod())) {
                breakfastStart = config.getStartTime();
            } else if ("LUNCH".equals(config.getMealPeriod())) {
                lunchStart = config.getStartTime();
            } else if ("DINNER".equals(config.getMealPeriod())) {
                dinnerStart = config.getStartTime();
            }
        }

        return MealPeriod.getCurrentPeriod(LocalTime.now(), breakfastStart, lunchStart, dinnerStart);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveConfigs(Long familyId, List<MealPeriodConfig> configs) {
        for (MealPeriodConfig config : configs) {
            config.setFamilyId(familyId);
            saveOrUpdateConfig(config);
        }
    }
}
