package com.yao.food_menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yao.food_menu.entity.DailyMealPublishItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 餐次发布菜品记录Mapper
 */
@Mapper
public interface DailyMealPublishItemMapper extends BaseMapper<DailyMealPublishItem> {
}
