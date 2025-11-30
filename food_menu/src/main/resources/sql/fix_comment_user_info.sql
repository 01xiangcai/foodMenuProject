-- 修复评论表设计：添加被回复人ID字段，支持实时获取用户信息
-- 执行时间：2025-12-01

-- 1. 添加 reply_to_user_id 字段
ALTER TABLE `dish_comment` 
ADD COLUMN `reply_to_user_id` bigint NULL DEFAULT NULL COMMENT '被回复人用户ID' AFTER `reply_to_name`;

-- 2. 根据现有数据填充 reply_to_user_id（根据 parent_id 查找被回复人的 wx_user_id）
UPDATE `dish_comment` dc1
INNER JOIN `dish_comment` dc2 ON dc1.parent_id = dc2.id
SET dc1.reply_to_user_id = dc2.wx_user_id
WHERE dc1.parent_id IS NOT NULL AND dc1.reply_to_user_id IS NULL;

-- 3. 为 reply_to_user_id 添加索引（可选，提高查询性能）
CREATE INDEX `idx_reply_to_user_id` ON `dish_comment`(`reply_to_user_id`);

