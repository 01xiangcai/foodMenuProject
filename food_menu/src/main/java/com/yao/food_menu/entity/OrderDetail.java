package com.yao.food_menu.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order detail entity
 */
@Data
public class OrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    // Order id
    private Long orderId;

    // Dish id
    private Long dishId;

    // Dish name
    private String name;

    // Image
    private String image;

    // Quantity
    private Integer number;

    // Amount
    private BigDecimal amount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
