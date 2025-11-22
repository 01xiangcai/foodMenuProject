package com.yao.food_menu.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Dish comment entity
 */
@Data
public class DishComment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long dishId;

    private Long parentId;

    private Long userId;

    private String authorName;

    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

