package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.common.exception.BusinessException;
import com.yao.food_menu.dto.PrizeConfigDto;
import com.yao.food_menu.entity.ActivityPrize;
import com.yao.food_menu.mapper.ActivityPrizeMapper;
import com.yao.food_menu.service.ActivityPrizeService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 活动奖品服务实现类
 */
@Service
public class ActivityPrizeServiceImpl extends ServiceImpl<ActivityPrizeMapper, ActivityPrize>
        implements ActivityPrizeService {

    @Override
    public Long addPrize(Long activityId, PrizeConfigDto dto) {
        ActivityPrize prize = new ActivityPrize();
        BeanUtils.copyProperties(dto, prize);
        prize.setActivityId(activityId);

        // 如果总数量不是无限,设置剩余数量
        if (prize.getTotalQuantity() != -1) {
            prize.setRemainQuantity(prize.getTotalQuantity());
        }

        this.save(prize);
        return prize.getId();
    }

    @Override
    public void updatePrize(Long prizeId, PrizeConfigDto dto) {
        ActivityPrize prize = this.getById(prizeId);
        if (prize == null) {
            throw new BusinessException("奖品不存在");
        }

        BeanUtils.copyProperties(dto, prize);
        this.updateById(prize);
    }

    @Override
    public void deletePrize(Long prizeId) {
        this.removeById(prizeId);
    }

    @Override
    public List<PrizeConfigDto> getActivityPrizes(Long activityId) {
        LambdaQueryWrapper<ActivityPrize> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityPrize::getActivityId, activityId)
                .orderByAsc(ActivityPrize::getPrizeLevel);

        List<ActivityPrize> prizes = this.list(wrapper);

        return prizes.stream()
                .map(prize -> {
                    PrizeConfigDto dto = new PrizeConfigDto();
                    BeanUtils.copyProperties(prize, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ActivityPrize drawPrize(Long activityId) {
        // 获取活动的所有奖品
        LambdaQueryWrapper<ActivityPrize> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityPrize::getActivityId, activityId)
                .orderByAsc(ActivityPrize::getPrizeLevel);

        List<ActivityPrize> prizes = this.list(wrapper);

        if (prizes.isEmpty()) {
            throw new BusinessException("活动暂无奖品");
        }

        // 过滤掉库存为0的奖品
        List<ActivityPrize> availablePrizes = prizes.stream()
                .filter(p -> p.getRemainQuantity() == null ||
                        p.getRemainQuantity() == -1 ||
                        p.getRemainQuantity() > 0)
                .collect(Collectors.toList());

        if (availablePrizes.isEmpty()) {
            throw new BusinessException("奖品已抽完");
        }

        // 计算总概率
        BigDecimal totalProbability = availablePrizes.stream()
                .map(ActivityPrize::getWinProbability)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 生成随机数
        double random = Math.random();
        BigDecimal randomValue = BigDecimal.valueOf(random).multiply(totalProbability);

        // 确定中奖奖品
        BigDecimal cumulative = BigDecimal.ZERO;
        for (ActivityPrize prize : availablePrizes) {
            cumulative = cumulative.add(prize.getWinProbability());
            if (randomValue.compareTo(cumulative) <= 0) {
                return prize;
            }
        }

        // 兜底返回最后一个奖品
        return availablePrizes.get(availablePrizes.size() - 1);
    }

    @Override
    public void decreaseStock(Long prizeId) {
        ActivityPrize prize = this.getById(prizeId);
        if (prize == null) {
            throw new BusinessException("奖品不存在");
        }

        // 如果不是无限库存,扣减库存
        if (prize.getRemainQuantity() != null && prize.getRemainQuantity() != -1) {
            if (prize.getRemainQuantity() <= 0) {
                throw new BusinessException("奖品库存不足");
            }
            prize.setRemainQuantity(prize.getRemainQuantity() - 1);
            this.updateById(prize);
        }
    }
}
