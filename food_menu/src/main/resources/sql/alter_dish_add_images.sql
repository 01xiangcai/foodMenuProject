-- ===================================================
-- 菜品多图功能数据库迁移脚本
-- ===================================================

-- 1. 添加 local_images 字段
ALTER TABLE dish ADD COLUMN local_images TEXT COMMENT '所有图片路径JSON数组' AFTER local_image;

-- 2. 数据迁移：将现有 local_image 转换为 JSON 数组格式
UPDATE dish 
SET local_images = CONCAT('["', local_image, '"]')
WHERE local_image IS NOT NULL AND local_image != '';

-- 3. 创建系统配置表
CREATE TABLE IF NOT EXISTS system_config (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  config_key VARCHAR(100) NOT NULL COMMENT '配置键',
  config_value VARCHAR(500) NOT NULL COMMENT '配置值',
  description VARCHAR(200) COMMENT '配置说明',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 4. 插入默认配置
INSERT INTO system_config (config_key, config_value, description, create_time, update_time) 
VALUES ('dish_image_limit', '5', '菜品图片数量限制', NOW(), NOW())
ON DUPLICATE KEY UPDATE config_value = '5';

