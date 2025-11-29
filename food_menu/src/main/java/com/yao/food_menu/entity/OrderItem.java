package com.yao.food_menu.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order item entity
 */
@Data
@TableName("order_items")
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    // Order id
    private Long orderId;

    // Family id
    private Long familyId;

    // Dish id
    private Long dishId;

    // Dish name
    private String dishName;

    // Dish image
    @TableField("dish_image")
    private String dishImage;

    // Price
    private BigDecimal price;

    // Quantity
    private Integer quantity;

    // Subtotal
    private BigDecimal subtotal;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
