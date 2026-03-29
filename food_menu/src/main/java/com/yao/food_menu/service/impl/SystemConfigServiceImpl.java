package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.entity.SystemConfig;
import com.yao.food_menu.mapper.SystemConfigMapper;
import com.yao.food_menu.service.SystemConfigService;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * 系统配置 Service 实现类
 */
@Service
@Slf4j
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    @Override
    public String getConfigValue(String configKey) {
        LambdaQueryWrapper<SystemConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemConfig::getConfigKey, configKey)
                    .orderByDesc(SystemConfig::getUpdateTime); // 优先获取最新更新的
        
        java.util.List<SystemConfig> configs = this.list(queryWrapper);
        if (configs == null || configs.isEmpty()) {
            log.info("SystemConfig: 未找到键为 [{}] 的配置", configKey);
            return null;
        }

        if (configs.size() > 1) {
            log.warn("SystemConfig: 键 [{}] 发现 {} 条记录！请清理冗余数据。", configKey, configs.size());
            for (SystemConfig c : configs) {
                log.info("  - ID: {}, Value: {}, FamilyId: {}, UpdateTime: {}", 
                    c.getId(), c.getConfigValue(), c.getFamilyId(), c.getUpdateTime());
            }
        }

        // 策略：优先选择 familyId 为空的记录（系统全局配置），如果没有则选择最新的一条
        SystemConfig target = configs.stream()
            .filter(c -> c.getFamilyId() == null)
            .findFirst()
            .orElse(configs.get(0));

        log.info("SystemConfig: 键 [{}] 最终选取值: {}, 来自 ID: {} (FamilyId: {})", 
            configKey, target.getConfigValue(), target.getId(), target.getFamilyId());
            
        return target.getConfigValue();
    }

    @Override
    public boolean updateConfigValue(String configKey, String configValue) {
        // 优先查找全局配置（familyId 为 null）
        LambdaQueryWrapper<SystemConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemConfig::getConfigKey, configKey)
                    .isNull(SystemConfig::getFamilyId);
        
        SystemConfig config = this.getOne(queryWrapper, false);
        
        if (config != null) {
            log.info("SystemConfig: 更新现有全局配置 [{}], ID: {}", configKey, config.getId());
            config.setConfigValue(configValue);
            return this.updateById(config);
        } else {
            log.info("SystemConfig: 全局配置 [{}] 不存在，尝试创建新记录", configKey);
            SystemConfig newConfig = new SystemConfig();
            newConfig.setConfigKey(configKey);
            newConfig.setConfigValue(configValue);
            newConfig.setFamilyId(null);
            return this.save(newConfig);
        }
    }
}

