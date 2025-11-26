package com.yao.food_menu.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品实体类
 */
@Data
public class Dish implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    // 菜品名称
    private String name;

    // 分类ID
    private Long categoryId;

    // 价格
    private BigDecimal price;

    // 图片编码
    private String code;

    // 图片路径
    private String image;

    // 描述
    private String description;

    // 状态: 0-停售, 1-起售
    private Integer status;

    // 排序
    private Integer sort;

    // 卡路里
    private String calories;

    // 标签
    private String tags;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    // 逻辑删除
    @TableLogic
    private Integer isDeleted;
}
