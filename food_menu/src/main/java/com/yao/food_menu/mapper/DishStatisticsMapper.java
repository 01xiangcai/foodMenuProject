package com.yao.food_menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yao.food_menu.entity.DishStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 菜品统计Mapper接口
 */
@Mapper
public interface DishStatisticsMapper extends BaseMapper<DishStatistics> {

    /**
     * 增加菜品点单次数
     * 
     * @param dishId 菜品ID
     * @return 影响行数
     */
    @Update("INSERT INTO dish_statistics (dish_id, total_order_count, last_order_time) " +
            "VALUES (#{dishId}, 1, NOW()) " +
            "ON DUPLICATE KEY UPDATE " +
            "total_order_count = total_order_count + 1, " +
            "last_order_time = NOW()")
    int incrementOrderCount(@Param("dishId") Long dishId);

    /**
     * 校验并修复统计数据
     * 
     * @return 影响行数
     */
    @Update("INSERT INTO dish_statistics (dish_id, total_order_count, last_order_time) " +
            "SELECT oi.dish_id, COUNT(*) as total_count, MAX(o.create_time) as last_time " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "WHERE o.status IN (2, 3) " +
            "GROUP BY oi.dish_id " +
            "ON DUPLICATE KEY UPDATE " +
            "total_order_count = VALUES(total_order_count), " +
            "last_order_time = VALUES(last_order_time)")
    int verifyAndFixStatistics();
}
