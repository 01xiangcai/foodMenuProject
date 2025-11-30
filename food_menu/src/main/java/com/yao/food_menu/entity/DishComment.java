package com.yao.food_menu.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
    // 被回复人昵称（保留用于历史记录，查询时优先使用实时数据）
    private String replyToName;
    // 被回复人用户ID（用于实时获取最新用户信息）
    private Long replyToUserId;
    // 评论内容
    private String content;

    // 家庭ID
    private Long familyId;

    // 逻辑删除: 0-未删除, 1-已删除
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
