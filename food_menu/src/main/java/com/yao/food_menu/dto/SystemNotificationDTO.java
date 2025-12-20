package com.yao.food_menu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统通知DTO
 */
@Data
public class SystemNotificationDTO {

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 通知类型编码
     */
    private String typeCode;

    /**
     * 通知类型名称
     */
    private String typeName;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 图标URL
     */
    private String icon;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 阅读时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

    /**
     * 额外数据（JSON格式，用于跳转等）
     */
    private String extra;
}
