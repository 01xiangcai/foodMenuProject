package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.dto.NotificationTypeConfigDTO;
import com.yao.food_menu.entity.NotificationTypeConfig;
import com.yao.food_menu.mapper.NotificationTypeConfigMapper;
import com.yao.food_menu.service.NotificationTypeConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知类型配置服务实现
 */
@Service
@Slf4j
public class NotificationTypeConfigServiceImpl extends ServiceImpl<NotificationTypeConfigMapper, NotificationTypeConfig>
        implements NotificationTypeConfigService {

    @Override
    public List<NotificationTypeConfigDTO> getAllConfigs() {
        LambdaQueryWrapper<NotificationTypeConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(NotificationTypeConfig::getSortOrder);
        List<NotificationTypeConfig> configs = list(wrapper);

        return configs.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public NotificationTypeConfig getByCode(String code) {
        return baseMapper.selectByCode(code);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addConfig(NotificationTypeConfigDTO dto) {
        // 检查编码是否已存在
        NotificationTypeConfig existing = getByCode(dto.getCode());
        if (existing != null) {
            throw new RuntimeException("通知类型编码已存在: " + dto.getCode());
        }

        NotificationTypeConfig config = new NotificationTypeConfig();
        BeanUtils.copyProperties(dto, config);
        config.setIsSystem(NotificationTypeConfig.SYSTEM_NO); // 自定义类型
        config.setIsEnabled(NotificationTypeConfig.ENABLED_YES); // 默认启用

        save(config);
        log.info("新增通知类型配置: code={}, name={}", dto.getCode(), dto.getName());
        return config.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(Long id, NotificationTypeConfigDTO dto) {
        NotificationTypeConfig config = getById(id);
        if (config == null) {
            throw new RuntimeException("通知类型配置不存在");
        }

        // 不允许修改编码
        config.setName(dto.getName());
        config.setTitleTemplate(dto.getTitleTemplate());
        config.setContentTemplate(dto.getContentTemplate());
        config.setIcon(dto.getIcon());
        config.setSortOrder(dto.getSortOrder());

        updateById(config);
        log.info("更新通知类型配置: id={}, name={}", id, dto.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleEnabled(Long id) {
        NotificationTypeConfig config = getById(id);
        if (config == null) {
            throw new RuntimeException("通知类型配置不存在");
        }

        // 切换启用状态
        config.setIsEnabled(config.getIsEnabled() == NotificationTypeConfig.ENABLED_YES
                ? NotificationTypeConfig.ENABLED_NO
                : NotificationTypeConfig.ENABLED_YES);

        updateById(config);
        log.info("切换通知类型状态: id={}, isEnabled={}", id, config.getIsEnabled());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long id) {
        NotificationTypeConfig config = getById(id);
        if (config == null) {
            throw new RuntimeException("通知类型配置不存在");
        }

        // 系统预置类型不可删除
        if (config.getIsSystem() == NotificationTypeConfig.SYSTEM_YES) {
            throw new RuntimeException("系统预置通知类型不可删除");
        }

        removeById(id);
        log.info("删除通知类型配置: id={}, code={}", id, config.getCode());
    }

    /**
     * 转换为DTO
     */
    private NotificationTypeConfigDTO toDTO(NotificationTypeConfig config) {
        NotificationTypeConfigDTO dto = new NotificationTypeConfigDTO();
        BeanUtils.copyProperties(config, dto);
        return dto;
    }
}
