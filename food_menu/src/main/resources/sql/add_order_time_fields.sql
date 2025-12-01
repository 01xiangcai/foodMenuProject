-- 为订单表添加接单、配送、完成时间字段
ALTER TABLE `orders` 
ADD COLUMN `accept_time` datetime NULL DEFAULT NULL COMMENT '接单时间' AFTER `status`,
ADD COLUMN `delivery_time` datetime NULL DEFAULT NULL COMMENT '配送时间' AFTER `accept_time`,
ADD COLUMN `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间' AFTER `delivery_time`;

