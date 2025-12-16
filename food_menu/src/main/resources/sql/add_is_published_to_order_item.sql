-- 在order_items表添加is_published字段
ALTER TABLE `order_items` 
ADD COLUMN `is_published` TINYINT DEFAULT 0 COMMENT '是否被发布 0-未发布 1-已发布';
