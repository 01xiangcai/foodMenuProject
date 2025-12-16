package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.entity.DailyMealPublishItem;

import java.util.List;

/**
 * 餐次发布菜品记录Service
 */
public interface DailyMealPublishItemService extends IService<DailyMealPublishItem> {

    /**
     * 批量插入发布记录
     * 
     * @param publishItems 发布记录列表
     */
    void batchInsert(List<DailyMealPublishItem> publishItems);

    /**
     * 根据大订单ID查询发布记录
     * 
     * @param dailyMealOrderId 大订单ID
     * @return 发布记录列表
     */
    List<DailyMealPublishItem> getByDailyMealOrderId(Long dailyMealOrderId);
}
