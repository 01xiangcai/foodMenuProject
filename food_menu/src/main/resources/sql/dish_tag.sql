-- ===================================================
-- 菜品标签配置表
-- ===================================================

SET NAMES utf8mb4;

-- ----------------------------
-- Table structure for dish_tag
-- ----------------------------
DROP TABLE IF EXISTS `dish_tag`;
CREATE TABLE `dish_tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签名称',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签图标(Emoji)',
  `type` int NULL DEFAULT 5 COMMENT '标签类型: 1-菜系, 2-口味, 3-特点, 4-分类, 5-其他',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` int NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_user` bigint NULL DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name` ASC) USING BTREE COMMENT '标签名称唯一索引',
  INDEX `idx_type`(`type` ASC) USING BTREE COMMENT '类型索引',
  INDEX `idx_status`(`status` ASC) USING BTREE COMMENT '状态索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜品标签配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 插入初始标签数据
-- ----------------------------
INSERT INTO `dish_tag` (`name`, `icon`, `type`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES
-- 菜系类 (type=1)
('川菜', '🌶️', 1, 1, 1, NOW(), NOW(), 1, 1),
('湘菜', '🔥', 1, 2, 1, NOW(), NOW(), 1, 1),
('粤菜', '🥘', 1, 3, 1, NOW(), NOW(), 1, 1),
('鲁菜', '🍖', 1, 4, 1, NOW(), NOW(), 1, 1),
('浙菜', '🦐', 1, 5, 1, NOW(), NOW(), 1, 1),
('东北菜', '🥟', 1, 6, 1, NOW(), NOW(), 1, 1),
('本帮菜', '🍲', 1, 7, 1, NOW(), NOW(), 1, 1),

-- 口味类 (type=2)
('辣', '🌶️', 2, 1, 1, NOW(), NOW(), 1, 1),
('麻辣', '🔥', 2, 2, 1, NOW(), NOW(), 1, 1),
('酸辣', '🍋', 2, 3, 1, NOW(), NOW(), 1, 1),
('酸甜', '🍯', 2, 4, 1, NOW(), NOW(), 1, 1),
('甜', '🍬', 2, 5, 1, NOW(), NOW(), 1, 1),
('清淡', '🌿', 2, 6, 1, NOW(), NOW(), 1, 1),
('蒜香', '🧄', 2, 7, 1, NOW(), NOW(), 1, 1),
('鲜香', '✨', 2, 8, 1, NOW(), NOW(), 1, 1),
('香脆', '⚡', 2, 9, 1, NOW(), NOW(), 1, 1),

-- 特点类 (type=3)
('下饭', '🍚', 3, 1, 1, NOW(), NOW(), 1, 1),
('家常', '🏠', 3, 2, 1, NOW(), NOW(), 1, 1),
('经典', '⭐', 3, 3, 1, NOW(), NOW(), 1, 1),
('招牌', '👑', 3, 4, 1, NOW(), NOW(), 1, 1),
('特色', '💎', 3, 5, 1, NOW(), NOW(), 1, 1),
('素食', '🥬', 3, 6, 1, NOW(), NOW(), 1, 1),
('健康', '💚', 3, 7, 1, NOW(), NOW(), 1, 1),
('营养', '🥗', 3, 8, 1, NOW(), NOW(), 1, 1),
('养生', '☘️', 3, 9, 1, NOW(), NOW(), 1, 1),
('儿童最爱', '👶', 3, 10, 1, NOW(), NOW(), 1, 1),
('开胃', '😋', 3, 11, 1, NOW(), NOW(), 1, 1),
('暖胃', '🫖', 3, 12, 1, NOW(), NOW(), 1, 1),
('滋补', '🍵', 3, 13, 1, NOW(), NOW(), 1, 1),

-- 分类类 (type=4)
('海鲜', '🦞', 4, 1, 1, NOW(), NOW(), 1, 1),
('烧烤', '🍢', 4, 2, 1, NOW(), NOW(), 1, 1),
('主食', '🍜', 4, 3, 1, NOW(), NOW(), 1, 1),
('点心', '🥟', 4, 4, 1, NOW(), NOW(), 1, 1),
('甜品', '🍰', 4, 5, 1, NOW(), NOW(), 1, 1);

