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
 * 分类实体类
 */
@Data
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    // 类型: 1-菜品分类, 2-套餐分类
    private Integer type;

    // 分类名称
    private String name;

    // 家庭ID
    private Long familyId;

    // 排序
    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    // 逻辑删除: 0-未删除, 1-已删除
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

    @TableField(exist = false)
    private Long dishCount;
}
