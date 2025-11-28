package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.entity.DishStatistics;
import java.util.List;
import java.util.Map;

/**
 * 菜品统计服务接口
 */
public interface DishStatisticsService extends IService<DishStatistics> {

    /**
     * 增加菜品点单次数
     * 
     * @param dishId 菜品ID
     */
    void incrementOrderCount(Long dishId);

    /**
     * 批量增加菜品点单次数
     * 
     * @param dishIds 菜品ID列表
     */
    void batchIncrementOrderCount(List<Long> dishIds);

    /**
     * 根据菜品ID获取统计信息
     * 
     * @param dishId 菜品ID
     * @return 统计信息
     */
    DishStatistics getByDishId(Long dishId);

    /**
     * 批量获取菜品统计信息
     * 
     * @param dishIds 菜品ID列表
     * @return 菜品ID -> 统计信息的映射
     */
    Map<Long, DishStatistics> getBatchByDishIds(List<Long> dishIds);

    /**
     * 校验并修复统计数据
     * 从订单表重新计算所有菜品的点单次数,并更新到统计表
     * 
     * @return 修复的菜品数量
     */
    int verifyAndFixStatistics();
}
