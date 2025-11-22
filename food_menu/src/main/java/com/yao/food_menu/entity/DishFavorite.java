package com.yao.food_menu.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Dish favorite entity
 */
@Data
public class DishFavorite implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    // User id
    private Long userId;

    // Dish id
    private Long dishId;

    // Create time (favorite time)
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

