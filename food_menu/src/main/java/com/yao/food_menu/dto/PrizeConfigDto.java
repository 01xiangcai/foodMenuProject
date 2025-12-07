package com.yao.food_menu.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 奖品配置DTO
 */
@Data
public class PrizeConfigDto {

    private Long id;

    // 奖品名称
    private String prizeName;

    // 奖品类型
    private String prizeType;

    // 奖品图片
    private String prizeImage;

    // 奖品价值
    private BigDecimal prizeValue;

    // 奖品配置
    private String prizeConfig;

    // 总数量
    private Integer totalQuantity;

    // 剩余数量
    private Integer remainQuantity;

    // 中奖概率
    private BigDecimal winProbability;

    // 奖品等级
    private Integer prizeLevel;
}
