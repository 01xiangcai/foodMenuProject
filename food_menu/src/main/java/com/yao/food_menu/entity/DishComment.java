package com.yao.food_menu.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 菜品评论实体类
 */
@Data
public class DishComment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    // 菜品ID
    private Long dishId;

    // 父评论ID
    private Long parentId;

    // 微信用户ID
    private Long wxUserId;

    // 作者名称
    private String authorName;

    // 作者头像URL
    private String avatarUrl;
    // 被回复人昵称
    private String replyToName;
    // 评论内容
    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
