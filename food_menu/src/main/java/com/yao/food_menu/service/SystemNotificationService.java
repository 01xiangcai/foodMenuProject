package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.dto.SystemNotificationDTO;
import com.yao.food_menu.entity.SystemNotification;

import java.util.Map;

/**
 * 系统通知服务接口
 */
public interface SystemNotificationService extends IService<SystemNotification> {

    /**
     * 根据类型编码发送通知给单个用户
     *
     * @param userId   接收用户ID
     * @param familyId 家庭ID
     * @param typeCode 通知类型编码
     * @param params   模板参数
     */
    void sendByType(Long userId, Long familyId, String typeCode, Map<String, Object> params);

    /**
     * 根据类型编码发送通知给家庭所有成员
     *
     * @param familyId 家庭ID
     * @param typeCode 通知类型编码
     * @param params   模板参数
     */
    void sendToFamilyByType(Long familyId, String typeCode, Map<String, Object> params);

    /**
     * 获取用户通知列表（分页）
     */
    Page<SystemNotificationDTO> getUserNotifications(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户未读通知数量
     */
    Integer getUnreadCount(Long userId);

    /**
     * 标记单条通知为已读
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 标记用户所有通知为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 发送系统公告
     *
     * @param familyId 家庭ID，为null表示全平台
     * @param title    标题
     * @param content  内容
     */
    void sendSystemAnnounce(Long familyId, String title, String content);
}
