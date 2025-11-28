package com.yao.food_menu.task;

import com.yao.food_menu.service.DishStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 菜品统计相关定时任务
 */
@Component
@Slf4j
public class DishStatisticsTask {

    @Autowired
    private DishStatisticsService dishStatisticsService;

    /**
     * 每天凌晨2点校验并修复统计数据
     * 确保统计数据的准确性，修正可能因并发或其他原因导致的数据不一致
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void verifyStatistics() {
        log.info("开始执行菜品统计数据校验定时任务");
        try {
            int affectedRows = dishStatisticsService.verifyAndFixStatistics();
            log.info("菜品统计数据校验定时任务执行完成，修复了 {} 条记录", affectedRows);
        } catch (Exception e) {
            log.error("菜品统计数据校验定时任务执行失败", e);
        }
    }
}
