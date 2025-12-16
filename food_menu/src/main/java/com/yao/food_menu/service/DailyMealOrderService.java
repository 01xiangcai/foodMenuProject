package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.entity.DailyMealOrder;

import java.time.LocalDate;
import java.util.List;

/**
 * 大订单服务
 */
public interface DailyMealOrderService extends IService<DailyMealOrder> {

    /**
     * 创建或获取大订单
     * 
     * @param familyId   家庭ID
     * @param orderDate  订单日期
     * @param mealPeriod 餐次
     * @return 大订单
     */
    DailyMealOrder createOrGet(Long familyId, LocalDate orderDate, String mealPeriod);

    /**
     * 更新大订单统计信息
     * 
     * @param dailyMealOrderId 大订单ID
     */
    void updateStatistics(Long dailyMealOrderId);

    /**
     * 确认发布大订单
     * 
     * @param dailyMealOrderId 大订单ID
     * @param confirmedBy      确认人ID
     */
    /**
     * 确认发布大订单
     * 
     * @param dailyMealOrderId 大订单ID
     * @param confirmedBy      确认人 ID
     * @param dishIds          选中的菜品ID列表,如果为null则发布所有菜品
     */
    void confirmOrder(Long dailyMealOrderId, Long confirmedBy, List<Long> dishIds);

    /**
     * 获取家庭某天的所有大订单
     */
    List<DailyMealOrder> getByFamilyAndDate(Long familyId, LocalDate orderDate);

    /**
     * 获取大订单详情(包含所有小订单)
     */
    DailyMealOrder getDetailById(Long dailyMealOrderId);

    /**
     * 查询家庭历史大订单
     */
    List<DailyMealOrder> getHistoryOrders(Long familyId, LocalDate startDate, LocalDate endDate);

    /**
     * 检查订单是否可以修改/取消
     */
    boolean canModifyOrder(Long dailyMealOrderId);
}
