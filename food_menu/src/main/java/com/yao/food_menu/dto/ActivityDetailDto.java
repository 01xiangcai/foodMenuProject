package com.yao.food_menu.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 活动详情DTO
 */
@Data
public class ActivityDetailDto {

    private Long id;

    // 活动名称
    private String activityName;

    // 活动类型
    private String activityType;

    // 活动描述
    private String activityDesc;

    // 活动横幅图片
    private String bannerImage;

    // 开始时间
    private LocalDateTime startTime;

    // 结束时间
    private LocalDateTime endTime;

    // 状态
    private Integer status;

    // 参与次数限制
    private Integer participateLimit;

    // 限制类型
    private String limitType;

    // 参与条件配置
    private String participateCondition;

    // 活动配置
    private String activityConfig;

    // 排序权重
    private Integer sortOrder;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;
}
