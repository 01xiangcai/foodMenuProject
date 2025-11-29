-- 为所有表添加逻辑删除字段(deleted)
-- 注意：如果表已经有is_deleted字段，需要先统一为deleted字段

-- 1. user表 - 添加deleted字段
ALTER TABLE `user` ADD COLUMN `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除' AFTER `role`;

-- 2. category表 - 添加deleted字段
ALTER TABLE `category` ADD COLUMN `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除' AFTER `family_id`;

-- 3. dish_comment表 - 添加deleted字段
ALTER TABLE `dish_comment` ADD COLUMN `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除' AFTER `family_id`;

-- 4. dish_favorite表 - 添加deleted字段
ALTER TABLE `dish_favorite` ADD COLUMN `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除' AFTER `family_id`;

-- 5. dish_statistics表 - 添加deleted字段
ALTER TABLE `dish_statistics` ADD COLUMN `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除' AFTER `family_id`;

-- 6. dish_tag表 - 添加deleted字段
ALTER TABLE `dish_tag` ADD COLUMN `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除' AFTER `family_id`;

-- 7. family表 - 添加deleted字段
ALTER TABLE `family` ADD COLUMN `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除' AFTER `update_user`;

-- 8. order_items表 - 添加deleted字段
ALTER TABLE `order_items` ADD COLUMN `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除' AFTER `family_id`;

-- 9. orders表 - 添加deleted字段
ALTER TABLE `orders` ADD COLUMN `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除' AFTER `family_id`;

-- 10. system_config表 - 添加deleted字段
ALTER TABLE `system_config` ADD COLUMN `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除' AFTER `update_time`;

-- 重要：确保所有现有数据的deleted字段值为0（而不是NULL）
-- 如果deleted字段为NULL，MyBatis-Plus的逻辑删除可能会过滤掉这些记录
UPDATE `user` SET `deleted` = 0 WHERE `deleted` IS NULL;
UPDATE `category` SET `deleted` = 0 WHERE `deleted` IS NULL;
UPDATE `dish_comment` SET `deleted` = 0 WHERE `deleted` IS NULL;
UPDATE `dish_favorite` SET `deleted` = 0 WHERE `deleted` IS NULL;
UPDATE `dish_statistics` SET `deleted` = 0 WHERE `deleted` IS NULL;
UPDATE `dish_tag` SET `deleted` = 0 WHERE `deleted` IS NULL;
UPDATE `family` SET `deleted` = 0 WHERE `deleted` IS NULL;
UPDATE `order_items` SET `deleted` = 0 WHERE `deleted` IS NULL;
UPDATE `orders` SET `deleted` = 0 WHERE `deleted` IS NULL;
UPDATE `system_config` SET `deleted` = 0 WHERE `deleted` IS NULL;

-- 注意：以下表已经有逻辑删除字段，但字段名不同，需要统一
-- banner表有is_deleted字段，需要重命名为deleted（可选，如果不想改可以保持is_deleted）
-- dish表有is_deleted字段，需要重命名为deleted（可选）
-- dish_flavor表有is_deleted字段，需要重命名为deleted（可选）

-- 如果需要统一所有表使用deleted字段名，可以执行以下SQL（可选）：
-- ALTER TABLE `banner` CHANGE COLUMN `is_deleted` `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除';
-- ALTER TABLE `dish` CHANGE COLUMN `is_deleted` `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除';
-- ALTER TABLE `dish_flavor` CHANGE COLUMN `is_deleted` `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除';

