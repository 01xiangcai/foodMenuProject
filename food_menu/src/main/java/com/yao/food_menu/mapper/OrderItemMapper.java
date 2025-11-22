package com.yao.food_menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yao.food_menu.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * Order item mapper
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
