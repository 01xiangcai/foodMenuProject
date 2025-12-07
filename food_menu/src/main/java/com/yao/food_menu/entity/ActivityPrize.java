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
 * 活动奖品实体类
 */
@Data
public class ActivityPrize implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    // 活动ID
    private Long activityId;

    // 奖品名称
    private String prizeName;

    // 奖品类型: COUPON(优惠券), POINTS(积分), DISH(菜品), PHYSICAL(实物), THANK_YOU(谢谢参与)
    private String prizeType;

    // 奖品图片
    private String prizeImage;

    // 本地奖品图片路径
    private String localPrizeImage;

    // 奖品价值
    private BigDecimal prizeValue;

    // 奖品配置(如优惠券ID、菜品ID等)
    private String prizeConfig;

    // 总数量(-1表示无限)
    private Integer totalQuantity;

    // 剩余数量
    private Integer remainQuantity;

    // 中奖概率(0.0001-1.0000)
    private BigDecimal winProbability;

    // 奖品等级(用于排序展示)
    private Integer prizeLevel;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 逻辑删除: 0-未删除, 1-已删除
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
