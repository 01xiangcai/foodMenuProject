package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.entity.SystemConfig;
import com.yao.food_menu.mapper.SystemConfigMapper;
import com.yao.food_menu.service.SystemConfigService;
import org.springframework.stereotype.Service;

/**
 * 系统配置 Service 实现类
 */
@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    @Override
    public String getConfigValue(String configKey) {
        LambdaQueryWrapper<SystemConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemConfig::getConfigKey, configKey);
        SystemConfig config = this.getOne(queryWrapper);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public boolean updateConfigValue(String configKey, String configValue) {
        LambdaQueryWrapper<SystemConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemConfig::getConfigKey, configKey);
        SystemConfig config = this.getOne(queryWrapper);
        
        if (config != null) {
            // 更新现有配置
            LambdaUpdateWrapper<SystemConfig> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SystemConfig::getConfigKey, configKey)
                        .set(SystemConfig::getConfigValue, configValue);
            return this.update(updateWrapper);
        } else {
            // 创建新配置
            SystemConfig newConfig = new SystemConfig();
            newConfig.setConfigKey(configKey);
            newConfig.setConfigValue(configValue);
            return this.save(newConfig);
        }
    }
}

