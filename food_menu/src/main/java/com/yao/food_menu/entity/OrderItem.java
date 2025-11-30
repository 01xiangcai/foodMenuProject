package com.yao.food_menu.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
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

    // 逻辑删除: 0-未删除, 1-已删除
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

    // 菜品状态（临时字段，不持久化）：0-停售, 1-在售
    @TableField(exist = false)
    private Integer dishStatus;
}
