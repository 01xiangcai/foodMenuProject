-- ============================================
-- 系统通知功能数据库脚本
-- 创建时间: 2024-12-20
-- ============================================

-- 1. 通知类型配置表（管理员可配置）
CREATE TABLE IF NOT EXISTS `notification_type_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` VARCHAR(32) NOT NULL COMMENT '类型编码（唯一标识）',
  `name` VARCHAR(50) NOT NULL COMMENT '类型名称',
  `title_template` VARCHAR(100) NOT NULL COMMENT '标题模板',
  `content_template` VARCHAR(500) NOT NULL COMMENT '内容模板',
  `icon` VARCHAR(200) DEFAULT NULL COMMENT '图标URL',
  `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用: 0-禁用, 1-启用',
  `is_system` TINYINT DEFAULT 0 COMMENT '是否系统预置: 0-自定义, 1-预置（不可删除）',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知类型配置表';

-- 2. 预置通知类型数据
INSERT INTO `notification_type_config` (`code`, `name`, `title_template`, `content_template`, `is_system`, `sort_order`) VALUES
('MEAL_PUBLISHED', '餐次发布', '今日{mealPeriod}菜单已发布', '共{dishCount}道菜品，快来看看吧！', 1, 1),
('DISH_ACCEPTED', '菜品采纳', '您的菜品已被采纳', '您点的{dishName}已被采纳，即将为您准备', 1, 2),
('LATE_ORDER_ACCEPTED', '迟到订单采纳', '您的迟到订单已被接受', '您的迟到订单菜品「{dishName}」已被采纳', 1, 3),
('DISH_REJECTED', '菜品拒绝', '菜品未被采纳', '很抱歉，您点的{dishName}本次未被采纳', 1, 4),
('REFUND_SUCCESS', '退款成功', '退款到账通知', '{amount}元已退回您的钱包', 1, 5),
('MEAL_SERVED', '开饭通知', '开饭啦！', '今日{mealPeriod}已出餐，快来享用吧！', 1, 6),
('SYSTEM_ANNOUNCE', '系统公告', '{title}', '{content}', 1, 99)
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 3. 系统通知表（存储已发送通知）
CREATE TABLE IF NOT EXISTS `system_notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `family_id` BIGINT NOT NULL COMMENT '家庭ID',
  `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
  `type_code` VARCHAR(32) NOT NULL COMMENT '通知类型编码',
  `title` VARCHAR(100) NOT NULL COMMENT '通知标题（渲染后）',
  `content` VARCHAR(500) NOT NULL COMMENT '通知内容（渲染后）',
  `extra` JSON DEFAULT NULL COMMENT '扩展数据',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_family_id` (`family_id`),
  KEY `idx_type_code` (`type_code`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_user_unread` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';
