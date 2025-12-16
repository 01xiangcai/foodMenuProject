package com.yao.food_menu.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 餐次配置实体类
 */
@Data
@TableName("meal_period_config")
public class MealPeriodConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    // 家庭ID
    private Long familyId;

    // 餐次类型: BREAKFAST/LUNCH/DINNER
    private String mealPeriod;

    // 开始时间
    private LocalTime startTime;

    // 结束时间
    private LocalTime endTime;

    // 下单截止时间
    private LocalTime orderDeadline;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 逻辑删除: 0-未删除, 1-已删除
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
