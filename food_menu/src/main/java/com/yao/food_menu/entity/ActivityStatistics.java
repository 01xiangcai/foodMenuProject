package com.yao.food_menu.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 活动统计实体类
 */
@Data
public class ActivityStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    // 活动ID
    private Long activityId;

    // 统计日期
    private LocalDate statDate;

    // 总参与人数
    private Integer totalParticipants;

    // 总参与次数
    private Integer totalParticipations;

    // 总中奖人数
    private Integer totalWinners;

    // 总中奖次数
    private Integer totalWins;

    // 总奖品价值
    private BigDecimal totalPrizeValue;

    // 转化率
    private BigDecimal conversionRate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 逻辑删除: 0-未删除, 1-已删除
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
