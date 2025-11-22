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
 * Orders entity
 */
@Data
public class Orders implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    // Order number
    private String number;

    // User id
    private Long userId;

    // Delivery address
    private String address;

    // Consignee
    private String consignee;

    // Phone number
    private String phone;

    // Total amount
    private BigDecimal amount;

    // Order status: 1-pending payment, 2-paid, 3-completed, 4-cancelled
    private Integer status;

    // Remark
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
