package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.common.context.FamilyContext;
import com.yao.food_menu.common.exception.BusinessException;
import com.yao.food_menu.dto.ActivityCreateDto;
import com.yao.food_menu.dto.ActivityDetailDto;
import com.yao.food_menu.entity.ActivityPrize;
import com.yao.food_menu.entity.MarketingActivity;
import com.yao.food_menu.mapper.MarketingActivityMapper;
import com.yao.food_menu.service.ActivityPrizeService;
import com.yao.food_menu.service.MarketingActivityService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 营销活动服务实现类
 */
@Service
public class MarketingActivityServiceImpl extends ServiceImpl<MarketingActivityMapper, MarketingActivity>
        implements MarketingActivityService {

    private final ActivityPrizeService activityPrizeService;

    public MarketingActivityServiceImpl(ActivityPrizeService activityPrizeService) {
        this.activityPrizeService = activityPrizeService;
    }

    @Override
    public Long createActivity(ActivityCreateDto dto) {
        MarketingActivity activity = new MarketingActivity();
        BeanUtils.copyProperties(dto, activity);

        // 设置家庭ID
        activity.setFamilyId(FamilyContext.getCurrentFamilyId());

        // 设置初始状态为未开始
        activity.setStatus(0);

        // 保存活动
        this.save(activity);

        return activity.getId();
    }

    @Override
    public void updateActivity(Long id, ActivityCreateDto dto) {
        MarketingActivity activity = this.getById(id);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }

        BeanUtils.copyProperties(dto, activity);
        this.updateById(activity);
    }

    @Override
    public ActivityDetailDto getActivityDetail(Long id) {
        MarketingActivity activity = this.getById(id);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }

        ActivityDetailDto dto = new ActivityDetailDto();
        BeanUtils.copyProperties(activity, dto);

        // 加载奖品列表
        List<ActivityPrize> prizes = activityPrizeService.getPrizesByActivityId(id);
        dto.setPrizes(prizes);

        return dto;
    }

    @Override
    public Page<ActivityDetailDto> getActivityPage(Integer pageNum, Integer pageSize,
            String activityType, Integer status) {
        Page<MarketingActivity> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<MarketingActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(activityType != null, MarketingActivity::getActivityType, activityType)
                .eq(status != null, MarketingActivity::getStatus, status)
                .orderByDesc(MarketingActivity::getSortOrder)
                .orderByDesc(MarketingActivity::getCreateTime);

        Page<MarketingActivity> activityPage = this.page(page, wrapper);

        // 转换为DTO
        Page<ActivityDetailDto> dtoPage = new Page<>();
        BeanUtils.copyProperties(activityPage, dtoPage, "records");

        List<ActivityDetailDto> dtoList = activityPage.getRecords().stream()
                .map(activity -> {
                    ActivityDetailDto dto = new ActivityDetailDto();
                    BeanUtils.copyProperties(activity, dto);
                    return dto;
                })
                .collect(Collectors.toList());

        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public List<ActivityDetailDto> getActiveActivities() {
        LocalDateTime now = LocalDateTime.now();

        LambdaQueryWrapper<MarketingActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MarketingActivity::getStatus, 1) // 进行中
                .le(MarketingActivity::getStartTime, now)
                .ge(MarketingActivity::getEndTime, now)
                .orderByDesc(MarketingActivity::getSortOrder);

        List<MarketingActivity> activities = this.list(wrapper);

        return activities.stream()
                .map(activity -> {
                    ActivityDetailDto dto = new ActivityDetailDto();
                    BeanUtils.copyProperties(activity, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void updateActivityStatus(Long id, Integer status) {
        MarketingActivity activity = this.getById(id);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }

        activity.setStatus(status);
        this.updateById(activity);
    }

    @Override
    public void deleteActivity(Long id) {
        this.removeById(id);
    }
}
