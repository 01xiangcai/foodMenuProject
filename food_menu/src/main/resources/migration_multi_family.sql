-- =====================================================
-- 多家庭模式数据库迁移脚本
-- 创建时间: 2025-11-29
-- 说明: 将系统改造为多家庭（多租户）模式
-- =====================================================

-- 1. 创建家庭表 (family)
CREATE TABLE IF NOT EXISTS `family` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(100) NOT NULL COMMENT '家庭名称',
  `description` VARCHAR(500) COMMENT '家庭描述',
  `invite_code` VARCHAR(50) UNIQUE COMMENT '邀请码',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user` BIGINT COMMENT '创建人',
  `update_user` BIGINT COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invite_code` (`invite_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭表';

-- 2. 创建默认家庭
INSERT INTO `family` (`name`, `description`, `invite_code`, `status`) 
VALUES ('默认家庭', '系统默认家庭，用于迁移现有数据', 'DEFAULT001', 1);

-- 3. 获取默认家庭ID（用于后续数据迁移）
SET @default_family_id = LAST_INSERT_ID();

-- 4. 修改user表（管理员表）
-- 添加family_id和role字段
ALTER TABLE `user` 
ADD COLUMN `family_id` BIGINT COMMENT '家庭ID',
ADD COLUMN `role` TINYINT DEFAULT 0 COMMENT '角色: 0-普通管理员, 1-家庭管理员, 2-超级管理员';

-- 5. 修改wx_user表（小程序用户表）
-- 添加family_id字段
ALTER TABLE `wx_user` 
ADD COLUMN `family_id` BIGINT COMMENT '家庭ID';

-- 6. 修改dish表（菜品表）
-- 添加family_id字段
ALTER TABLE `dish` 
ADD COLUMN `family_id` BIGINT COMMENT '家庭ID';

-- 7. 修改orders表（订单表）
-- 添加family_id字段
ALTER TABLE `orders` 
ADD COLUMN `family_id` BIGINT COMMENT '家庭ID';

-- 8. 修改category表（分类表）
-- 添加family_id字段
ALTER TABLE `category` 
ADD COLUMN `family_id` BIGINT COMMENT '家庭ID';

-- 9. 修改banner表（轮播图表）
-- 添加family_id字段
ALTER TABLE `banner` 
ADD COLUMN `family_id` BIGINT COMMENT '家庭ID';

-- 10. 修改dish_comment表（菜品评论表）
-- 添加family_id字段
ALTER TABLE `dish_comment` 
ADD COLUMN `family_id` BIGINT COMMENT '家庭ID';

-- 11. 修改dish_favorite表（菜品收藏表）
-- 添加family_id字段
ALTER TABLE `dish_favorite` 
ADD COLUMN `family_id` BIGINT COMMENT '家庭ID';

-- 12. 修改dish_flavor表（菜品口味表）
-- 添加family_id字段
ALTER TABLE `dish_flavor` 
ADD COLUMN `family_id` BIGINT COMMENT '家庭ID';

-- 13. 修改dish_tag表（菜品标签表）
-- 添加family_id字段
ALTER TABLE `dish_tag` 
ADD COLUMN `family_id` BIGINT COMMENT '家庭ID';

-- 14. 修改dish_statistics表（菜品统计表）
-- 添加family_id字段
ALTER TABLE `dish_statistics` 
ADD COLUMN `family_id` BIGINT COMMENT '家庭ID';

-- 15. 将现有数据关联到默认家庭
-- 更新user表
UPDATE `user` SET `family_id` = @default_family_id WHERE `family_id` IS NULL;

-- 更新wx_user表
UPDATE `wx_user` SET `family_id` = @default_family_id WHERE `family_id` IS NULL;

-- 更新dish表
UPDATE `dish` SET `family_id` = @default_family_id WHERE `family_id` IS NULL;

-- 更新orders表
UPDATE `orders` SET `family_id` = @default_family_id WHERE `family_id` IS NULL;

-- 更新category表
UPDATE `category` SET `family_id` = @default_family_id WHERE `family_id` IS NULL;

-- 更新banner表
UPDATE `banner` SET `family_id` = @default_family_id WHERE `family_id` IS NULL;

-- 更新dish_comment表
UPDATE `dish_comment` SET `family_id` = @default_family_id WHERE `family_id` IS NULL;

-- 更新dish_favorite表
UPDATE `dish_favorite` SET `family_id` = @default_family_id WHERE `family_id` IS NULL;

-- 更新dish_flavor表
UPDATE `dish_flavor` SET `family_id` = @default_family_id WHERE `family_id` IS NULL;

-- 更新dish_tag表
UPDATE `dish_tag` SET `family_id` = @default_family_id WHERE `family_id` IS NULL;

-- 更新dish_statistics表
UPDATE `dish_statistics` SET `family_id` = @default_family_id WHERE `family_id` IS NULL;

-- 16. 添加索引以提高查询性能
-- user表索引
ALTER TABLE `user` ADD INDEX `idx_family_id` (`family_id`);

-- wx_user表索引
ALTER TABLE `wx_user` ADD INDEX `idx_family_id` (`family_id`);

-- dish表索引
ALTER TABLE `dish` ADD INDEX `idx_family_id` (`family_id`);

-- orders表索引
ALTER TABLE `orders` ADD INDEX `idx_family_id` (`family_id`);

-- category表索引
ALTER TABLE `category` ADD INDEX `idx_family_id` (`family_id`);

-- banner表索引
ALTER TABLE `banner` ADD INDEX `idx_family_id` (`family_id`);

-- dish_comment表索引
ALTER TABLE `dish_comment` ADD INDEX `idx_family_id` (`family_id`);

-- dish_favorite表索引
ALTER TABLE `dish_favorite` ADD INDEX `idx_family_id` (`family_id`);

-- dish_flavor表索引
ALTER TABLE `dish_flavor` ADD INDEX `idx_family_id` (`family_id`);

-- dish_tag表索引
ALTER TABLE `dish_tag` ADD INDEX `idx_family_id` (`family_id`);

-- dish_statistics表索引
ALTER TABLE `dish_statistics` ADD INDEX `idx_family_id` (`family_id`);

-- 17. 设置第一个用户为超级管理员
UPDATE `user` SET `role` = 2 WHERE `id` = 1;

-- =====================================================
-- 迁移完成
-- 请验证以下内容：
-- 1. family表已创建，默认家庭已插入
-- 2. 所有相关表已添加family_id字段
-- 3. 现有数据已关联到默认家庭
-- 4. 索引已创建
-- 5. 第一个用户已设置为超级管理员
-- =====================================================
