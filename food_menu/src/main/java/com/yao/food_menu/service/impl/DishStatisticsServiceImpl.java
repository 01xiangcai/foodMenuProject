package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.entity.DishStatistics;
import com.yao.food_menu.mapper.DishStatisticsMapper;
import com.yao.food_menu.service.DishStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜品统计服务实现类
 */
@Service
@Slf4j
public class DishStatisticsServiceImpl extends ServiceImpl<DishStatisticsMapper, DishStatistics>
        implements DishStatisticsService {

    @Override
    @Transactional
    public void incrementOrderCount(Long dishId) {
        log.info("增加菜品点单次数: dishId={}", dishId);
        baseMapper.incrementOrderCount(dishId);
    }

    @Override
    @Transactional
    public void batchIncrementOrderCount(List<Long> dishIds) {
        log.info("批量增加菜品点单次数: dishIds={}", dishIds);
        for (Long dishId : dishIds) {
            baseMapper.incrementOrderCount(dishId);
        }
    }

    @Override
    public DishStatistics getByDishId(Long dishId) {
        LambdaQueryWrapper<DishStatistics> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishStatistics::getDishId, dishId);
        return this.getOne(queryWrapper);
    }

    @Override
    public Map<Long, DishStatistics> getBatchByDishIds(List<Long> dishIds) {
        if (dishIds == null || dishIds.isEmpty()) {
            return Map.of();
        }

        LambdaQueryWrapper<DishStatistics> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DishStatistics::getDishId, dishIds);
        List<DishStatistics> statisticsList = this.list(queryWrapper);

        return statisticsList.stream()
                .collect(Collectors.toMap(DishStatistics::getDishId, stat -> stat));
    }

    @Override
    @Transactional
    public int verifyAndFixStatistics() {
        log.info("开始校验并修复菜品统计数据");

        try {
            // 调用Mapper执行校验和修复
            int affectedRows = baseMapper.verifyAndFixStatistics();

            log.info("菜品统计数据校验完成,影响行数: {}", affectedRows);
            return affectedRows;
        } catch (Exception e) {
            log.error("校验并修复菜品统计数据失败", e);
            throw new RuntimeException("校验统计数据失败: " + e.getMessage());
        }
    }
}
