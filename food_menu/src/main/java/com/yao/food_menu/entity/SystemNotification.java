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
 * 系统通知实体类
 * 存储已发送给用户的通知
 */
@Data
@TableName("system_notification")
public class SystemNotification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 家庭ID
     */
    private Long familyId;

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 通知类型编码
     */
    private String typeCode;

    /**
     * 通知标题（渲染后）
     */
    private String title;

    /**
     * 通知内容（渲染后）
     */
    private String content;

    /**
     * 扩展数据（JSON格式）
     */
    private String extra;

    /**
     * 是否已读: 0-未读, 1-已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 阅读时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

    // ========== 常量定义 ==========

    /**
     * 已读状态
     */
    public static final int READ_YES = 1;
    public static final int READ_NO = 0;
}
