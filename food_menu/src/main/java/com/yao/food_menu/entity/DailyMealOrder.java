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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 大订单实体类
 */
@Data
@TableName("daily_meal_order")
public class DailyMealOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    // 家庭ID
    private Long familyId;

    // 订单日期
    private LocalDate orderDate;

    // 餐次类型: BREAKFAST/LUNCH/DINNER
    private String mealPeriod;

    // 状态: 0-收集中, 1-已确认, 2-已截止
    private Integer status;

    // 总金额
    private BigDecimal totalAmount;

    // 参与人数
    private Integer memberCount;

    // 菜品数量
    private Integer dishCount;

    // 确认人ID
    private Long confirmedBy;

    // 确认时间
    private LocalDateTime confirmedTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 逻辑删除: 0-未删除, 1-已删除
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

    // ========== 常量定义 ==========

    // 状态常量
    public static final int STATUS_COLLECTING = 0; // 收集中
    public static final int STATUS_CONFIRMED = 1; // 已确认
    public static final int STATUS_EXPIRED = 2; // 已截止
}
