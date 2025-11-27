package com.yao.food_menu.dto;

import lombok.Data;

/**
 * 迁移状态DTO
 */
@Data
public class MigrationStatus {

    /**
     * 迁移类型: DISH, AVATAR
     */
    private String type;

    /**
     * 总数
     */
    private Integer total;

    /**
     * 已迁移数量
     */
    private Integer migrated;

    /**
     * 失败数量
     */
    private Integer failed;

    /**
     * 跳过数量(已存在本地图片)
     */
    private Integer skipped;

    /**
     * 是否正在运行
     */
    private Boolean running;

    /**
     * 进度百分比
     */
    private Double progress;

    /**
     * 状态消息
     */
    private String message;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    public Double getProgress() {
        if (total == null || total == 0) {
            return 0.0;
        }
        return (migrated + failed + skipped) * 100.0 / total;
    }
}
