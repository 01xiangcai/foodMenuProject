-- =====================================================
-- 今日菜单功能数据库迁移脚本
-- 版本: V1.0
-- 创建时间: 2025-12-16
-- 说明: 新增餐次配置表、大订单表,修改订单表
-- =====================================================

-- 1. 创建餐次配置表
CREATE TABLE IF NOT EXISTS `meal_period_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `family_id` BIGINT NOT NULL COMMENT '家庭ID',
  `meal_period` VARCHAR(20) NOT NULL COMMENT '餐次类型: BREAKFAST/LUNCH/DINNER',
  `start_time` TIME NOT NULL COMMENT '开始时间',
  `end_time` TIME NOT NULL COMMENT '结束时间',
  `order_deadline` TIME COMMENT '下单截止时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_family_period` (`family_id`, `meal_period`, `deleted`),
  KEY `idx_family_id` (`family_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='餐次配置表';

-- 2. 创建大订单表
CREATE TABLE IF NOT EXISTS `daily_meal_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `family_id` BIGINT NOT NULL COMMENT '家庭ID',
  `order_date` DATE NOT NULL COMMENT '订单日期',
  `meal_period` VARCHAR(20) NOT NULL COMMENT '餐次类型: BREAKFAST/LUNCH/DINNER',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-收集中, 1-已确认, 2-已截止',
  `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '总金额',
  `member_count` INT NOT NULL DEFAULT 0 COMMENT '参与人数',
  `dish_count` INT NOT NULL DEFAULT 0 COMMENT '菜品数量',
  `confirmed_by` BIGINT COMMENT '确认人ID',
  `confirmed_time` DATETIME COMMENT '确认时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_family_date_period` (`family_id`, `order_date`, `meal_period`, `deleted`),
  KEY `idx_family_id` (`family_id`),
  KEY `idx_order_date` (`order_date`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='大订单表';

-- 3. 修改订单表,新增餐次字段
-- 检查字段是否存在,避免重复添加
SET @dbname = DATABASE();
SET @tablename = 'orders';
SET @columnname1 = 'meal_period';
SET @columnname2 = 'daily_meal_order_id';
SET @preparedStatement1 = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname1)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname1, ' VARCHAR(20) COMMENT ''餐次类型: BREAKFAST/LUNCH/DINNER''')
));
PREPARE alterIfNotExists1 FROM @preparedStatement1;
EXECUTE alterIfNotExists1;
DEALLOCATE PREPARE alterIfNotExists1;

SET @preparedStatement2 = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname2)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname2, ' BIGINT COMMENT ''大订单ID''')
));
PREPARE alterIfNotExists2 FROM @preparedStatement2;
EXECUTE alterIfNotExists2;
DEALLOCATE PREPARE alterIfNotExists2;

-- 添加索引(如果不存在)
SET @preparedStatement3 = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (index_name = 'idx_daily_meal_order_id')
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD KEY idx_daily_meal_order_id (daily_meal_order_id)')
));
PREPARE alterIfNotExists3 FROM @preparedStatement3;
EXECUTE alterIfNotExists3;
DEALLOCATE PREPARE alterIfNotExists3;

-- 4. 新增系统配置(如果不存在)
INSERT INTO `system_config` (`config_key`, `config_value`, `description`, `family_id`)
SELECT 'meal.history.retention.days', '30', '历史数据保留天数', NULL
WHERE NOT EXISTS (SELECT 1 FROM `system_config` WHERE `config_key` = 'meal.history.retention.days');

INSERT INTO `system_config` (`config_key`, `config_value`, `description`, `family_id`)
SELECT 'meal.default.breakfast.time', '06:00-10:00', '默认早餐时段', NULL
WHERE NOT EXISTS (SELECT 1 FROM `system_config` WHERE `config_key` = 'meal.default.breakfast.time');

INSERT INTO `system_config` (`config_key`, `config_value`, `description`, `family_id`)
SELECT 'meal.default.lunch.time', '10:00-14:00', '默认中餐时段', NULL
WHERE NOT EXISTS (SELECT 1 FROM `system_config` WHERE `config_key` = 'meal.default.lunch.time');

INSERT INTO `system_config` (`config_key`, `config_value`, `description`, `family_id`)
SELECT 'meal.default.dinner.time', '14:00-06:00', '默认晚餐时段', NULL
WHERE NOT EXISTS (SELECT 1 FROM `system_config` WHERE `config_key` = 'meal.default.dinner.time');

-- 5. 插入默认餐次配置(为所有现有家庭)
INSERT INTO `meal_period_config` (`family_id`, `meal_period`, `start_time`, `end_time`, `order_deadline`)
SELECT f.id, 'BREAKFAST', '06:00:00', '10:00:00', '09:00:00'
FROM `family` f
WHERE NOT EXISTS (
  SELECT 1 FROM `meal_period_config` mpc 
  WHERE mpc.family_id = f.id AND mpc.meal_period = 'BREAKFAST' AND mpc.deleted = 0
);

INSERT INTO `meal_period_config` (`family_id`, `meal_period`, `start_time`, `end_time`, `order_deadline`)
SELECT f.id, 'LUNCH', '10:00:00', '14:00:00', '13:00:00'
FROM `family` f
WHERE NOT EXISTS (
  SELECT 1 FROM `meal_period_config` mpc 
  WHERE mpc.family_id = f.id AND mpc.meal_period = 'LUNCH' AND mpc.deleted = 0
);

INSERT INTO `meal_period_config` (`family_id`, `meal_period`, `start_time`, `end_time`, `order_deadline`)
SELECT f.id, 'DINNER', '14:00:00', '06:00:00', '20:00:00'
FROM `family` f
WHERE NOT EXISTS (
  SELECT 1 FROM `meal_period_config` mpc 
  WHERE mpc.family_id = f.id AND mpc.meal_period = 'DINNER' AND mpc.deleted = 0
);

-- =====================================================
-- 迁移完成
-- =====================================================
