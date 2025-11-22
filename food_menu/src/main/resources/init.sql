-- ====================================
-- 家庭点餐小程序数据库初始化脚本
-- ====================================

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 使用数据库
USE food_menu;

-- ====================================
-- 1. 用户表
-- ====================================
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
  `password` VARCHAR(200) DEFAULT NULL COMMENT '密码(加密)',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `name` VARCHAR(50) DEFAULT NULL COMMENT '姓名',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像',
  `status` INT DEFAULT 1 COMMENT '状态 0:禁用 1:正常',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_phone` (`phone`),
  UNIQUE KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ====================================
-- 2. 分类表
-- ====================================
CREATE TABLE IF NOT EXISTS `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `type` INT DEFAULT NULL COMMENT '类型 1:菜品分类 2:套餐分类',
  `name` VARCHAR(64) NOT NULL COMMENT '分类名称',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '顺序',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_user` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_user` BIGINT DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_category_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- ====================================
-- 3. 菜品表
-- ====================================
CREATE TABLE IF NOT EXISTS `dish` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(64) NOT NULL COMMENT '菜品名称',
  `category_id` BIGINT NOT NULL COMMENT '分类id',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `code` VARCHAR(64) DEFAULT NULL COMMENT '商品码',
  `image` VARCHAR(500) NOT NULL COMMENT '图片',
  `description` VARCHAR(400) DEFAULT NULL COMMENT '描述信息',
  `status` INT NOT NULL DEFAULT 1 COMMENT '0:停售 1:起售',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '顺序',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_user` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_user` BIGINT DEFAULT NULL COMMENT '修改人',
  `is_deleted` INT NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品表';

-- ====================================
-- 4. 菜品口味表
-- ====================================
CREATE TABLE IF NOT EXISTS `dish_flavor` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dish_id` BIGINT NOT NULL COMMENT '菜品id',
  `name` VARCHAR(64) NOT NULL COMMENT '口味名称',
  `value` VARCHAR(500) DEFAULT NULL COMMENT '口味数据list',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_user` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_user` BIGINT DEFAULT NULL COMMENT '修改人',
  `is_deleted` INT NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品口味表';

-- ====================================
-- 4.1 菜品评论表
-- ====================================
CREATE TABLE IF NOT EXISTS `dish_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dish_id` BIGINT NOT NULL COMMENT '菜品id',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
  `author_name` VARCHAR(50) DEFAULT NULL COMMENT '展示昵称',
  `content` VARCHAR(500) NOT NULL COMMENT '内容',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品评论表';

-- ====================================
-- 5. 订单表
-- ====================================
CREATE TABLE IF NOT EXISTS `orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `number` VARCHAR(50) NOT NULL COMMENT '订单号',
  `user_id` BIGINT NOT NULL COMMENT '用户id',
  `address` VARCHAR(200) DEFAULT NULL COMMENT '收货地址',
  `consignee` VARCHAR(50) DEFAULT NULL COMMENT '收货人',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  `status` INT DEFAULT 1 COMMENT '订单状态 1:待支付 2:已支付 3:已完成 4:已取消',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_number` (`number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ====================================
-- 6. 订单明细表
-- ====================================
CREATE TABLE IF NOT EXISTS `order_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` BIGINT NOT NULL COMMENT '订单id',
  `dish_id` BIGINT NOT NULL COMMENT '菜品id',
  `name` VARCHAR(50) NOT NULL COMMENT '菜品名称',
  `image` VARCHAR(500) DEFAULT NULL COMMENT '图片',
  `number` INT NOT NULL COMMENT '数量',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '金额',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ====================================
-- 7. 轮播图表
-- ====================================
CREATE TABLE IF NOT EXISTS `banner` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `image` VARCHAR(500) NOT NULL COMMENT '轮播图图片URL',
  `title` VARCHAR(100) DEFAULT NULL COMMENT '轮播图标题',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '轮播图描述',
  `link_url` VARCHAR(500) DEFAULT NULL COMMENT '链接地址',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` INT DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_user` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_user` BIGINT DEFAULT NULL COMMENT '修改人',
  `is_deleted` INT DEFAULT 0 COMMENT '是否删除 0:未删除 1:已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- ====================================
-- 初始化测试数据
-- ====================================

-- 清空现有数据(如果需要重新初始化)
TRUNCATE TABLE `banner`;
TRUNCATE TABLE `order_detail`;
TRUNCATE TABLE `orders`;
TRUNCATE TABLE `dish_flavor`;
TRUNCATE TABLE `dish`;
TRUNCATE TABLE `category`;
TRUNCATE TABLE `user`;

-- 插入分类数据
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES
(1, 1, '川菜', 1, NOW(), NOW(), 1, 1),
(2, 1, '湘菜', 2, NOW(), NOW(), 1, 1),
(3, 1, '粤菜', 3, NOW(), NOW(), 1, 1),
(4, 1, '家常菜', 4, NOW(), NOW(), 1, 1),
(5, 1, '主食', 5, NOW(), NOW(), 1, 1);

-- 插入菜品数据
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `code`, `image`, `description`, `status`, `sort`, `create_time`, `update_time`, `create_user`, `update_user`, `is_deleted`) VALUES
(1, '宫保鸡丁', 1, 38.00, 'DISH001', 'https://example.com/gongbao.jpg', '经典川菜,鸡肉鲜嫩,花生酥脆', 1, 1, NOW(), NOW(), 1, 1, 0),
(2, '麻婆豆腐', 1, 28.00, 'DISH002', 'https://example.com/mapo.jpg', '麻辣鲜香,豆腐嫩滑', 1, 2, NOW(), NOW(), 1, 1, 0),
(3, '剁椒鱼头', 2, 68.00, 'DISH003', 'https://example.com/yutou.jpg', '湘菜名菜,鲜辣开胃', 1, 3, NOW(), NOW(), 1, 1, 0),
(4, '白切鸡', 3, 48.00, 'DISH004', 'https://example.com/baiqieji.jpg', '粤菜经典,皮爽肉滑', 1, 4, NOW(), NOW(), 1, 1, 0),
(5, '番茄炒蛋', 4, 18.00, 'DISH005', 'https://example.com/fanqie.jpg', '家常小炒,酸甜可口', 1, 5, NOW(), NOW(), 1, 1, 0),
(6, '米饭', 5, 2.00, 'DISH006', 'https://example.com/rice.jpg', '香喷喷的米饭', 1, 6, NOW(), NOW(), 1, 1, 0);

-- 插入菜品口味数据
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`, `create_time`, `update_time`, `create_user`, `update_user`, `is_deleted`) VALUES
(1, 1, '辣度', '[\"不辣\",\"微辣\",\"中辣\",\"特辣\"]', NOW(), NOW(), 1, 1, 0),
(2, 2, '辣度', '[\"不辣\",\"微辣\",\"中辣\",\"特辣\"]', NOW(), NOW(), 1, 1, 0),
(3, 3, '辣度', '[\"微辣\",\"中辣\",\"特辣\"]', NOW(), NOW(), 1, 1, 0);

-- 插入测试用户
INSERT INTO `user` (`id`, `username`, `password`, `phone`, `name`, `avatar`, `status`, `create_time`, `update_time`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13800138000', '管理员', NULL, 1, NOW(), NOW()),
(2, 'test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13800138001', '测试用户', NULL, 1, NOW(), NOW());
-- 注意: 密码为 BCrypt 加密后的 "123456"

-- 插入轮播图数据
INSERT INTO `banner` (`id`, `image`, `title`, `description`, `link_url`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `is_deleted`) VALUES
(1, 'https://dummyimage.com/800x400/6366f1/ffffff&text=Banner+1', '家庭美食精选', '每日新鲜食材，用心烹饪每一道菜', '/pages/menu/menu', 1, 1, NOW(), NOW(), 1, 1, 0),
(2, 'https://dummyimage.com/800x400/8b5cf6/ffffff&text=Banner+2', '温馨家宴', '与家人共享美好时光', '/pages/menu/menu', 2, 1, NOW(), NOW(), 1, 1, 0),
(3, 'https://dummyimage.com/800x400/ec4899/ffffff&text=Banner+3', '健康饮食', '营养搭配，健康每一天', '/pages/menu/menu', 3, 1, NOW(), NOW(), 1, 1, 0);

-- ====================================
-- 完成初始化
-- ====================================
