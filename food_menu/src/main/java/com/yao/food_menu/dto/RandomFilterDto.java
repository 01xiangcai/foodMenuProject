package com.yao.food_menu.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 随机点餐筛选条件DTO
 */
@Data
public class RandomFilterDto implements Serializable {

    private static final long serialVersionUID = 1L;

    // 最低价格
    private BigDecimal priceMin;

    // 最高价格
    private BigDecimal priceMax;

    // 分类ID列表
    private List<Long> categoryIds;

    // 标签列表
    private List<String> tags;

    // 排除的菜品ID列表(用于去重)
    private List<Long> excludeIds;
}
