package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yao.food_menu.common.websocket.WebSocketMessage;
import com.yao.food_menu.common.websocket.WebSocketSessionManager;
import com.yao.food_menu.dto.SystemNotificationDTO;
import com.yao.food_menu.entity.NotificationTypeConfig;
import com.yao.food_menu.entity.SystemNotification;
import com.yao.food_menu.entity.WxUser;
import com.yao.food_menu.mapper.SystemNotificationMapper;
import com.yao.food_menu.service.NotificationTypeConfigService;
import com.yao.food_menu.service.SystemNotificationService;
import com.yao.food_menu.service.WxUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统通知服务实现
 */
@Service
@Slf4j
public class SystemNotificationServiceImpl extends ServiceImpl<SystemNotificationMapper, SystemNotification>
        implements SystemNotificationService {

    @Autowired
    private NotificationTypeConfigService typeConfigService;

    @Autowired
    private WxUserService wxUserService;

    @Autowired
    private WebSocketSessionManager sessionManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendByType(Long userId, Long familyId, String typeCode, Map<String, Object> params) {
        // 1. 获取通知类型配置
        NotificationTypeConfig config = typeConfigService.getByCode(typeCode);
        if (config == null) {
            log.warn("通知类型不存在: {}", typeCode);
            return;
        }

        // 2. 检查是否启用
        if (config.getIsEnabled() != NotificationTypeConfig.ENABLED_YES) {
            log.debug("通知类型已禁用，跳过发送: {}", typeCode);
            return;
        }

        // 3. 渲染模板
        String title = renderTemplate(config.getTitleTemplate(), params);
        String content = renderTemplate(config.getContentTemplate(), params);

        // 4. 保存通知
        SystemNotification notification = new SystemNotification();
        notification.setUserId(userId);
        notification.setFamilyId(familyId);
        notification.setTypeCode(typeCode);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setExtra(toJson(params));
        notification.setIsRead(SystemNotification.READ_NO);
        save(notification);

        log.info("发送通知: userId={}, type={}, title={}", userId, typeCode, title);

        // 5. 实时推送（异步）
        pushToUserAsync(userId, notification, config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendToFamilyByType(Long familyId, String typeCode, Map<String, Object> params) {
        // 获取家庭所有成员
        LambdaQueryWrapper<WxUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WxUser::getFamilyId, familyId);
        List<WxUser> members = wxUserService.list(wrapper);

        if (members.isEmpty()) {
            log.warn("家庭没有成员，无法发送通知: familyId={}", familyId);
            return;
        }

        log.info("向家庭发送通知: familyId={}, type={}, memberCount={}", familyId, typeCode, members.size());

        for (WxUser member : members) {
            sendByType(member.getId(), familyId, typeCode, params);
        }
    }

    @Override
    public Page<SystemNotificationDTO> getUserNotifications(Long userId, Integer pageNum, Integer pageSize) {
        Page<SystemNotification> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SystemNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemNotification::getUserId, userId);
        wrapper.orderByDesc(SystemNotification::getCreateTime);

        Page<SystemNotification> resultPage = page(page, wrapper);

        // 转换为DTO
        Page<SystemNotificationDTO> dtoPage = new Page<>(pageNum, pageSize, resultPage.getTotal());
        dtoPage.setRecords(resultPage.getRecords().stream().map(this::toDTO).toList());

        return dtoPage;
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        return baseMapper.countUnreadByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long notificationId, Long userId) {
        SystemNotification notification = getById(notificationId);
        if (notification == null) {
            throw new RuntimeException("通知不存在");
        }

        // 验证通知属于该用户
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此通知");
        }

        if (notification.getIsRead() == SystemNotification.READ_NO) {
            notification.setIsRead(SystemNotification.READ_YES);
            notification.setReadTime(LocalDateTime.now());
            updateById(notification);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        int count = baseMapper.markAllAsReadByUserId(userId);
        log.info("标记所有通知为已读: userId={}, count={}", userId, count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendSystemAnnounce(Long familyId, String title, String content) {
        Map<String, Object> params = new HashMap<>();
        params.put("title", title);
        params.put("content", content);

        if (familyId != null) {
            // 发送给特定家庭
            sendToFamilyByType(familyId, NotificationTypeConfig.CODE_SYSTEM_ANNOUNCE, params);
        } else {
            // 发送给所有用户
            List<WxUser> allUsers = wxUserService.list();
            for (WxUser user : allUsers) {
                sendByType(user.getId(), user.getFamilyId(), NotificationTypeConfig.CODE_SYSTEM_ANNOUNCE, params);
            }
            log.info("发送全平台公告: title={}, userCount={}", title, allUsers.size());
        }
    }

    /**
     * 渲染模板：将{key}替换为params中的value
     */
    private String renderTemplate(String template, Map<String, Object> params) {
        if (template == null || params == null) {
            return template;
        }

        String result = template;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue() != null ? String.valueOf(entry.getValue()) : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }

    /**
     * 异步推送通知给用户
     */
    @Async
    public void pushToUserAsync(Long userId, SystemNotification notification, NotificationTypeConfig config) {
        try {
            // 创建WebSocket消息
            Map<String, Object> data = new HashMap<>();
            data.put("id", notification.getId());
            data.put("typeCode", notification.getTypeCode());
            data.put("typeName", config.getName());
            data.put("title", notification.getTitle());
            data.put("content", notification.getContent());
            data.put("icon", config.getIcon());
            data.put("createTime", notification.getCreateTime());

            WebSocketMessage wsMessage = new WebSocketMessage(WebSocketMessage.TYPE_SYSTEM_NOTIFICATION, data);
            String messageJson = toJson(wsMessage);

            boolean sent = sessionManager.sendToUser(userId, messageJson);
            if (sent) {
                log.debug("实时推送通知成功: userId={}", userId);
            }
        } catch (Exception e) {
            log.error("实时推送通知失败: userId={}", userId, e);
        }
    }

    /**
     * 转换为DTO
     */
    private SystemNotificationDTO toDTO(SystemNotification notification) {
        SystemNotificationDTO dto = new SystemNotificationDTO();
        dto.setId(notification.getId());
        dto.setTypeCode(notification.getTypeCode());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setIsRead(notification.getIsRead() == SystemNotification.READ_YES);
        dto.setCreateTime(notification.getCreateTime());
        dto.setReadTime(notification.getReadTime());
        dto.setExtra(notification.getExtra()); // 添加extra字段用于跳转

        // 获取类型信息
        NotificationTypeConfig config = typeConfigService.getByCode(notification.getTypeCode());
        if (config != null) {
            dto.setTypeName(config.getName());
            dto.setIcon(config.getIcon());
        }

        return dto;
    }

    /**
     * 将对象转换为JSON字符串
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            return "{}";
        }
    }
}
