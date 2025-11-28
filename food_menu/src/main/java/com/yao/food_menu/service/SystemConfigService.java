package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.entity.SystemConfig;

/**
 * 系统配置 Service 接口
 */
public interface SystemConfigService extends IService<SystemConfig> {

    /**
     * 根据配置键获取配置值
     * @param configKey 配置键
     * @return 配置值，如果不存在返回null
     */
    String getConfigValue(String configKey);

    /**
     * 更新配置值
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 是否成功
     */
    boolean updateConfigValue(String configKey, String configValue);
}

