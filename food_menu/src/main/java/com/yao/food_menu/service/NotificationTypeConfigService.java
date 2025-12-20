package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.dto.NotificationTypeConfigDTO;
import com.yao.food_menu.entity.NotificationTypeConfig;

import java.util.List;

/**
 * 通知类型配置服务接口
 */
public interface NotificationTypeConfigService extends IService<NotificationTypeConfig> {

    /**
     * 获取所有通知类型配置
     */
    List<NotificationTypeConfigDTO> getAllConfigs();

    /**
     * 根据编码获取配置
     */
    NotificationTypeConfig getByCode(String code);

    /**
     * 新增通知类型
     */
    Long addConfig(NotificationTypeConfigDTO dto);

    /**
     * 更新通知类型
     */
    void updateConfig(Long id, NotificationTypeConfigDTO dto);

    /**
     * 切换启用状态
     */
    void toggleEnabled(Long id);

    /**
     * 删除通知类型（仅允许删除非系统预置）
     */
    void deleteConfig(Long id);
}
