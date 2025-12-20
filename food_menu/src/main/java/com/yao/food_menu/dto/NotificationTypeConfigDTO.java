package com.yao.food_menu.dto;

import lombok.Data;

/**
 * 通知类型配置DTO
 */
@Data
public class NotificationTypeConfigDTO {

    /**
     * 配置ID
     */
    private Long id;

    /**
     * 类型编码
     */
    private String code;

    /**
     * 类型名称
     */
    private String name;

    /**
     * 标题模板
     */
    private String titleTemplate;

    /**
     * 内容模板
     */
    private String contentTemplate;

    /**
     * 图标URL
     */
    private String icon;

    /**
     * 是否启用
     */
    private Integer isEnabled;

    /**
     * 是否系统预置
     */
    private Integer isSystem;

    /**
     * 排序
     */
    private Integer sortOrder;
}
