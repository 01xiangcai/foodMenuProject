package com.yao.food_menu.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知类型配置实体类
 * 用于管理员配置各种通知类型的模板
 */
@Data
@TableName("notification_type_config")
public class NotificationTypeConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 类型编码（唯一标识）
     */
    private String code;

    /**
     * 类型名称
     */
    private String name;

    /**
     * 标题模板
     * 支持变量替换，如：今日{mealPeriod}菜单已发布
     */
    private String titleTemplate;

    /**
     * 内容模板
     * 支持变量替换，如：共{dishCount}道菜品
     */
    private String contentTemplate;

    /**
     * 图标URL
     */
    private String icon;

    /**
     * 是否启用: 0-禁用, 1-启用
     */
    private Integer isEnabled;

    /**
     * 是否系统预置: 0-自定义, 1-预置（不可删除）
     */
    private Integer isSystem;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // ========== 常量定义 ==========

    /**
     * 启用状态
     */
    public static final int ENABLED_YES = 1;
    public static final int ENABLED_NO = 0;

    /**
     * 系统预置
     */
    public static final int SYSTEM_YES = 1;
    public static final int SYSTEM_NO = 0;

    // ========== 预置通知类型编码 ==========

    public static final String CODE_MEAL_PUBLISHED = "MEAL_PUBLISHED";
    public static final String CODE_MEAL_UPDATED = "MEAL_UPDATED";
    public static final String CODE_DISH_ACCEPTED = "DISH_ACCEPTED";
    public static final String CODE_LATE_ORDER_ACCEPTED = "LATE_ORDER_ACCEPTED";
    public static final String CODE_DISH_REJECTED = "DISH_REJECTED";
    public static final String CODE_PAYMENT_SUCCESS = "PAYMENT_SUCCESS";
    public static final String CODE_REFUND_SUCCESS = "REFUND_SUCCESS";
    public static final String CODE_MEAL_SERVED = "MEAL_SERVED";
    public static final String CODE_SYSTEM_ANNOUNCE = "SYSTEM_ANNOUNCE";
}
