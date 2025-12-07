-- =====================================================
-- 订单表支付字段扩展
-- 创建日期: 2025-12-07
-- =====================================================

-- 订单表新增支付相关字段
ALTER TABLE `orders` 
ADD COLUMN `pay_method` TINYINT DEFAULT NULL COMMENT '支付方式: 1=余额支付, 2=模拟支付' AFTER `remark`,
ADD COLUMN `pay_status` TINYINT DEFAULT 0 COMMENT '支付状态: 0=未支付, 1=已支付, 2=已退款' AFTER `pay_method`,
ADD COLUMN `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间' AFTER `pay_status`;

-- 更新现有订单的支付状态（假设现有订单都是模拟支付已完成）
UPDATE `orders` SET `pay_method` = 2, `pay_status` = 1 WHERE `pay_status` IS NULL OR `pay_status` = 0;
