-- ============================================
-- 数据库迁移脚本: 添加 family_id 字段
-- 执行时间: 2025-12-07
-- 说明: 为 wx_user_wallet、wx_wallet_transaction 和 system_config 表添加 family_id 字段
-- ============================================

-- 1. 为 system_config 表添加 family_id 字段
ALTER TABLE `system_config` 
ADD COLUMN `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID' AFTER `deleted`;

-- 为 system_config 表添加索引
ALTER TABLE `system_config` 
ADD INDEX `idx_family_id`(`family_id` ASC) USING BTREE;

-- 2. 为 wx_user_wallet 表添加 family_id 字段
ALTER TABLE `wx_user_wallet` 
ADD COLUMN `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID' AFTER `pay_password_error_date`;

-- 为 wx_user_wallet 表添加索引
ALTER TABLE `wx_user_wallet` 
ADD INDEX `idx_family_id`(`family_id` ASC) USING BTREE;

-- 3. 为 wx_wallet_transaction 表添加 family_id 字段
ALTER TABLE `wx_wallet_transaction` 
ADD COLUMN `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID' AFTER `create_time`;

-- 为 wx_wallet_transaction 表添加索引
ALTER TABLE `wx_wallet_transaction` 
ADD INDEX `idx_family_id`(`family_id` ASC) USING BTREE;

-- ============================================
-- 数据迁移: 根据用户的 family_id 更新关联数据
-- ============================================

-- 更新 wx_user_wallet 表的 family_id (根据用户的 family_id)
UPDATE wx_user_wallet wuw 
INNER JOIN wx_user wu ON wuw.wx_user_id = wu.id 
SET wuw.family_id = wu.family_id
WHERE wuw.family_id IS NULL;

-- 更新 wx_wallet_transaction 表的 family_id (根据用户的 family_id)
UPDATE wx_wallet_transaction wwt 
INNER JOIN wx_user wu ON wwt.wx_user_id = wu.id 
SET wwt.family_id = wu.family_id
WHERE wwt.family_id IS NULL;

-- 注意: system_config 表的 family_id 需要手动设置，或者根据业务需求批量更新
-- 如果现有配置是全局的，可以保持 family_id 为 NULL 或设置为默认家庭
