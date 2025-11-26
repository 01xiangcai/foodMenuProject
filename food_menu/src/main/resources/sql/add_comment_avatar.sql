-- 为dish_comment表添加avatar_url字段
ALTER TABLE dish_comment ADD COLUMN avatar_url VARCHAR(255) COMMENT '评论者头像URL';
