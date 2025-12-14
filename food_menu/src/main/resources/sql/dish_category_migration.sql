/*
 菜品多分类功能 - 数据库迁移脚本
 
 功能说明:
 1. 创建 dish_category 中间表,实现菜品与分类的多对多关系
 2. 将现有 dish 表中的 category_id 数据迁移到中间表
 3. 添加必要的索引以优化查询性能
 
 执行时间: 2025-12-14
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for dish_category (菜品分类关联表)
-- ----------------------------
DROP TABLE IF EXISTS `dish_category`;
CREATE TABLE `dish_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dish_id` bigint NOT NULL COMMENT '菜品ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_user` bigint NULL DEFAULT NULL COMMENT '修改人',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dish_category` (`dish_id` ASC, `category_id` ASC, `deleted` ASC) USING BTREE COMMENT '菜品-分类唯一索引(包含删除标记)',
  INDEX `idx_dish_id` (`dish_id` ASC) USING BTREE COMMENT '菜品ID索引',
  INDEX `idx_category_id` (`category_id` ASC) USING BTREE COMMENT '分类ID索引',
  INDEX `idx_family_id` (`family_id` ASC) USING BTREE COMMENT '家庭ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜品分类关联表' ROW_FORMAT=Dynamic;

-- ----------------------------
-- 数据迁移: 将现有 dish 表中的 category_id 迁移到 dish_category 表
-- ----------------------------
INSERT INTO `dish_category` (`dish_id`, `category_id`, `family_id`, `create_time`, `update_time`, `create_user`, `update_user`, `deleted`)
SELECT 
  `id` AS `dish_id`,
  `category_id`,
  `family_id`,
  `create_time`,
  `update_time`,
  `create_user`,
  `update_user`,
  `deleted`
FROM `dish`
WHERE `category_id` IS NOT NULL 
  AND `deleted` = 0
  AND NOT EXISTS (
    -- 避免重复插入(如果脚本被多次执行)
    SELECT 1 FROM `dish_category` dc 
    WHERE dc.`dish_id` = `dish`.`id` 
      AND dc.`category_id` = `dish`.`category_id`
      AND dc.`deleted` = 0
  );

-- ----------------------------
-- 验证迁移结果
-- ----------------------------
-- 执行以下查询验证数据迁移是否成功:
-- SELECT COUNT(*) AS dish_count FROM dish WHERE category_id IS NOT NULL AND deleted = 0;
-- SELECT COUNT(*) AS dish_category_count FROM dish_category WHERE deleted = 0;
-- 两个数量应该相等

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 说明
-- ----------------------------
-- 注意: dish 表中的 category_id 字段暂时保留,不删除
-- 这样可以确保向后兼容性,如果需要回滚,数据仍然完整
-- 新功能将使用 dish_category 表来管理菜品与分类的关系
