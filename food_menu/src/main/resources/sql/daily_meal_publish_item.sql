-- 创建餐次发布菜品记录表
CREATE TABLE `daily_meal_publish_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `daily_meal_order_id` BIGINT NOT NULL COMMENT '大订单ID',
  `order_item_id` BIGINT NOT NULL COMMENT '订单项ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `dish_id` BIGINT NOT NULL COMMENT '菜品ID',
  `dish_name` VARCHAR(100) COMMENT '菜品名称',
  `dish_image` VARCHAR(255) COMMENT '菜品图片',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `price` DECIMAL(10,2) NOT NULL COMMENT '单价',
  `subtotal` DECIMAL(10,2) NOT NULL COMMENT '小计',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `user_nickname` VARCHAR(100) COMMENT '用户昵称',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_daily_meal_order` (`daily_meal_order_id`),
  INDEX `idx_order_item` (`order_item_id`),
  INDEX `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='餐次发布菜品记录表';
