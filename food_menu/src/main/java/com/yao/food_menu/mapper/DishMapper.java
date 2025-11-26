package com.yao.food_menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yao.food_menu.dto.DishDto;
import com.yao.food_menu.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {

    @Select("SELECT d.*, IFNULL(SUM(oi.quantity), 0) as sales " +
            "FROM dish d " +
            "LEFT JOIN order_items oi ON d.id = oi.dish_id " +
            "WHERE d.status = 1 AND d.is_deleted = 0 " +
            "GROUP BY d.id " +
            "ORDER BY sales DESC " +
            "LIMIT #{limit}")
    List<DishDto> selectTopSelling(int limit);
}
