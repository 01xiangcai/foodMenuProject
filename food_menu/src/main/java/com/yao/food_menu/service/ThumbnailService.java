package com.yao.food_menu.service;

import com.yao.food_menu.dto.MigrationStatus;

/**
 * 缩略图生成服务接口
 */
public interface ThumbnailService {

    /**
     * 为指定相对路径的图片生成缩略图
     * 如果缩略图已存在则直接返回其相对路径
     * 
     * @param relativePath 原图相对路径 (如 food-menu/2023-10-27/abc.jpg)
     * @return 缩略图相对路径 (如 food-menu/2023-10-27/abc_thumb.jpg)
     */
    String generateThumbnail(String relativePath);

    /**
     * 异步批量为所有历史图片生成缩略图
     */
    void batchGenerateThumbnails();

    /**
     * 获取当前缩略图处理状态
     * 
     * @return 状态信息
     */
    MigrationStatus getThumbnailStatus();

    /**
     * 检查并统计缺失缩略图的数量
     * 
     * @return 缺失数量
     */
    int countMissingThumbnails();
}
