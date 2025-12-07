package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.dto.ActivityCreateDto;
import com.yao.food_menu.dto.ActivityDetailDto;
import com.yao.food_menu.entity.MarketingActivity;

import java.util.List;

/**
 * 营销活动服务接口
 */
public interface MarketingActivityService extends IService<MarketingActivity> {

    /**
     * 创建活动
     */
    Long createActivity(ActivityCreateDto dto);

    /**
     * 更新活动
     */
    void updateActivity(Long id, ActivityCreateDto dto);

    /**
     * 获取活动详情
     */
    ActivityDetailDto getActivityDetail(Long id);

    /**
     * 分页查询活动列表(管理端)
     */
    Page<ActivityDetailDto> getActivityPage(Integer pageNum, Integer pageSize, String activityType, Integer status);

    /**
     * 获取进行中的活动列表(小程序端)
     */
    List<ActivityDetailDto> getActiveActivities();

    /**
     * 更新活动状态
     */
    void updateActivityStatus(Long id, Integer status);

    /**
     * 删除活动
     */
    void deleteActivity(Long id);
}
