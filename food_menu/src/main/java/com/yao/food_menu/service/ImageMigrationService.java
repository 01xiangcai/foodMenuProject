package com.yao.food_menu.service;

import com.yao.food_menu.dto.MigrationStatus;

/**
 * 图片迁移服务接口
 */
public interface ImageMigrationService {

    /**
     * 迁移所有菜品图片从OSS到本地
     * 
     * @return 迁移状态
     */
    MigrationStatus migrateDishImages();

    /**
     * 迁移所有用户头像从OSS到本地
     * 
     * @return 迁移状态
     */
    MigrationStatus migrateAvatars();

    /**
     * 获取菜品图片迁移状态
     * 
     * @return 迁移状态
     */
    MigrationStatus getDishMigrationStatus();

    /**
     * 获取头像迁移状态
     * 
     * @return 迁移状态
     */
    MigrationStatus getAvatarMigrationStatus();

    /**
     * 停止正在进行的迁移
     */
    void stopMigration();
}
