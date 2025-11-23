/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : food_menu

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 23/11/2025 21:29:38
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for banner
-- ----------------------------
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '轮播图图片URL',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '轮播图标题',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '轮播图描述',
  `link_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链接地址',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` int NULL DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_user` bigint NULL DEFAULT NULL COMMENT '修改人',
  `is_deleted` int NULL DEFAULT 0 COMMENT '是否删除 0:未删除 1:已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '轮播图表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of banner
-- ----------------------------
INSERT INTO `banner` VALUES (1, 'food-menu/2025-11-22/a3530ad2-1a61-4338-8ea4-d4779f70632d-pexels-valeriya-1639565.jpg', '家庭美食精选', '每日新鲜食材，用心烹饪每一道菜', '/pages/menu/menu', 1, 1, '2025-11-22 03:13:28', '2025-11-22 17:19:45', 1, NULL, 0);
INSERT INTO `banner` VALUES (2, 'food-menu/2025-11-22/187f8b2c-1051-4c04-b621-279d8073bea2-pexels-gu-ko-2150570603-31972203.jpg', '温馨家宴', '与家人共享美好时光', '/pages/menu/menu', 2, 1, '2025-11-22 03:13:28', '2025-11-22 17:19:54', 1, NULL, 0);
INSERT INTO `banner` VALUES (3, 'food-menu/2025-11-22/8743a675-69d5-4461-9039-a3db99df3097-pexels-gu-ko-2150570603-31987439.jpg', '健康饮食', '营养搭配，健康每一天', '/pages/menu/menu', 3, 1, '2025-11-22 03:13:28', '2025-11-22 17:20:05', 1, NULL, 0);
INSERT INTO `banner` VALUES (4, 'food-menu/2025-11-22/0dee277f-b66d-46c3-92ba-30c0f4fd4b1c-pexels-kyleroxas-2122294.jpg', '轻食', '22', NULL, 4, 1, '2025-11-22 17:27:07', '2025-11-22 17:27:07', NULL, NULL, 0);

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭',
  `type` int NULL DEFAULT NULL COMMENT '绫诲瀷 1:鑿滃搧鍒嗙被 2:濂楅?鍒嗙被',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鍒嗙被鍚嶇О',
  `sort` int NOT NULL DEFAULT 0 COMMENT '椤哄簭',
  `create_time` datetime NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `create_user` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜',
  `update_user` bigint NULL DEFAULT NULL COMMENT '淇?敼浜',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_category_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鍒嗙被琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, 1, '川菜', 1, '2025-11-21 14:48:37', '2025-11-21 14:48:37', 1, 1);
INSERT INTO `category` VALUES (2, 1, '湘菜', 2, '2025-11-21 14:48:37', '2025-11-21 14:48:37', 1, 1);
INSERT INTO `category` VALUES (3, 1, '粤菜', 3, '2025-11-21 14:48:37', '2025-11-21 14:48:37', 1, 1);
INSERT INTO `category` VALUES (4, 1, '家常菜', 4, '2025-11-21 14:48:37', '2025-11-21 14:48:37', 1, 1);
INSERT INTO `category` VALUES (5, 1, '主食', 5, '2025-11-21 14:48:37', '2025-11-21 14:48:37', 1, 1);
INSERT INTO `category` VALUES (6, 1, '特色', 6, '2025-11-21 19:31:51', '2025-11-21 19:31:51', NULL, NULL);

-- ----------------------------
-- Table structure for dish
-- ----------------------------
DROP TABLE IF EXISTS `dish`;
CREATE TABLE `dish`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鑿滃搧鍚嶇О',
  `category_id` bigint NOT NULL COMMENT '鍒嗙被id',
  `price` decimal(10, 2) NOT NULL COMMENT '浠锋牸',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鍟嗗搧鐮',
  `image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鍥剧墖',
  `description` varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鎻忚堪淇℃伅',
  `status` int NOT NULL DEFAULT 1 COMMENT '0:鍋滃敭 1:璧峰敭',
  `sort` int NOT NULL DEFAULT 0 COMMENT '椤哄簭',
  `create_time` datetime NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `create_user` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜',
  `update_user` bigint NULL DEFAULT NULL COMMENT '淇?敼浜',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '鏄?惁鍒犻櫎',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鑿滃搧琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dish
-- ----------------------------
INSERT INTO `dish` VALUES (1, '宫保鸡丁', 1, 99.00, 'DISH001', 'https://dummyimage.com/800x600/0f172a/ffffff&text=family+dish', '经典川菜,鸡肉鲜嫩,花生酥脆', 0, 1, '2025-11-21 14:48:37', '2025-11-21 19:30:03', 1, NULL, 0);
INSERT INTO `dish` VALUES (2, '麻婆豆腐', 1, 28.00, 'DISH002', 'https://dummyimage.com/800x600/0f172a/ffffff&text=family+dish', '麻辣鲜香,豆腐嫩滑', 1, 2, '2025-11-21 14:48:37', '2025-11-21 14:48:37', 1, 1, 0);
INSERT INTO `dish` VALUES (3, '剁椒鱼头', 2, 68.00, 'DISH003', 'https://dummyimage.com/800x600/0f172a/ffffff&text=family+dish', '湘菜名菜,鲜辣开胃', 1, 3, '2025-11-21 14:48:37', '2025-11-21 14:48:37', 1, 1, 0);
INSERT INTO `dish` VALUES (4, '白切鸡', 3, 48.00, 'DISH004', 'https://dummyimage.com/800x600/0f172a/ffffff&text=family+dish', '粤菜经典,皮爽肉滑', 1, 4, '2025-11-21 14:48:37', '2025-11-21 14:48:37', 1, 1, 0);
INSERT INTO `dish` VALUES (5, '番茄炒蛋', 4, 18.00, 'DISH005', 'https://dummyimage.com/800x600/0f172a/ffffff&text=family+dish', '家常小炒,酸甜可口', 1, 5, '2025-11-21 14:48:37', '2025-11-21 14:48:37', 1, 1, 0);
INSERT INTO `dish` VALUES (6, '米饭', 5, 2.00, 'DISH006', 'https://dummyimage.com/800x600/0f172a/ffffff&text=family+dish', '香喷喷的米饭', 1, 6, '2025-11-21 14:48:37', '2025-11-21 14:48:37', 1, 1, 0);
INSERT INTO `dish` VALUES (7, '22222', 1, 22.00, NULL, 'food-menu/2025-11-22/82f3058e-4e35-4d78-92ab-ff09d7d4f2a4-屏幕截图(4).png', '22222222', 1, 0, '2025-11-21 21:16:01', '2025-11-22 02:43:04', NULL, NULL, 0);
INSERT INTO `dish` VALUES (8, '特色啊', 6, 0.00, NULL, 'https://dummyimage.com/800x600/0f172a/ffffff&text=family+dish', '水水水水水水水水水水水水水', 1, 0, '2025-11-21 21:17:28', '2025-11-21 21:17:28', NULL, NULL, 0);

-- ----------------------------
-- Table structure for dish_comment
-- ----------------------------
DROP TABLE IF EXISTS `dish_comment`;
CREATE TABLE `dish_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dish_id` bigint NOT NULL COMMENT '菜品id',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父评论ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `author_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '展示昵称',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜品评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dish_comment
-- ----------------------------

-- ----------------------------
-- Table structure for dish_favorite
-- ----------------------------
DROP TABLE IF EXISTS `dish_favorite`;
CREATE TABLE `dish_favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `dish_id` bigint NOT NULL COMMENT '菜品ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_dish`(`user_id` ASC, `dish_id` ASC) USING BTREE COMMENT '用户-菜品唯一索引',
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE COMMENT '用户ID索引',
  INDEX `idx_dish_id`(`dish_id` ASC) USING BTREE COMMENT '菜品ID索引'
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜品收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dish_favorite
-- ----------------------------
INSERT INTO `dish_favorite` VALUES (1, 2, 7, '2025-11-22 21:05:07');
INSERT INTO `dish_favorite` VALUES (3, 2, 6, '2025-11-22 21:05:48');
INSERT INTO `dish_favorite` VALUES (4, 1, 4, '2025-11-22 21:06:36');
INSERT INTO `dish_favorite` VALUES (5, 1, 2, '2025-11-22 23:41:24');
INSERT INTO `dish_favorite` VALUES (6, 1, 7, '2025-11-23 00:49:20');

-- ----------------------------
-- Table structure for dish_flavor
-- ----------------------------
DROP TABLE IF EXISTS `dish_flavor`;
CREATE TABLE `dish_flavor`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭',
  `dish_id` bigint NOT NULL COMMENT '鑿滃搧id',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鍙ｅ懗鍚嶇О',
  `value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鍙ｅ懗鏁版嵁list',
  `create_time` datetime NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `create_user` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜',
  `update_user` bigint NULL DEFAULT NULL COMMENT '淇?敼浜',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '鏄?惁鍒犻櫎',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鑿滃搧鍙ｅ懗琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dish_flavor
-- ----------------------------
INSERT INTO `dish_flavor` VALUES (1, 1, '辣度', '[\"不辣\",\"微辣\",\"中辣\",\"特辣\"]', '2025-11-21 14:48:37', '2025-11-21 14:48:37', 1, 1, 1);
INSERT INTO `dish_flavor` VALUES (2, 2, '辣度', '[\"不辣\",\"微辣\",\"中辣\",\"特辣\"]', '2025-11-21 14:48:37', '2025-11-21 14:48:37', 1, 1, 0);
INSERT INTO `dish_flavor` VALUES (3, 3, '辣度', '[\"微辣\",\"中辣\",\"特辣\"]', '2025-11-21 14:48:37', '2025-11-21 14:48:37', 1, 1, 0);
INSERT INTO `dish_flavor` VALUES (4, 1, '口味', '[\"不辣\",\"微辣\",\"中辣\",\"特辣\"]', '2025-11-21 19:26:08', '2025-11-21 19:26:08', NULL, NULL, 1);
INSERT INTO `dish_flavor` VALUES (5, 1, '口味', '[\"不辣\",\"微辣\",\"中辣\",\"特辣\"]', '2025-11-21 19:30:03', '2025-11-21 19:30:03', NULL, NULL, 0);
INSERT INTO `dish_flavor` VALUES (6, 7, '口味', '[\"222\"]', '2025-11-21 21:16:01', '2025-11-21 21:16:01', NULL, NULL, 1);
INSERT INTO `dish_flavor` VALUES (7, 8, '口味', '[\"水水水水水水水水水水水\"]', '2025-11-21 21:17:28', '2025-11-21 21:17:28', NULL, NULL, 0);
INSERT INTO `dish_flavor` VALUES (8, 7, '口味', '[\"222\"]', '2025-11-21 22:51:13', '2025-11-21 22:51:13', NULL, NULL, 1);
INSERT INTO `dish_flavor` VALUES (9, 7, '口味', '[\"222\"]', '2025-11-22 02:43:04', '2025-11-22 02:43:04', NULL, NULL, 0);

-- ----------------------------
-- Table structure for order_items
-- ----------------------------
DROP TABLE IF EXISTS `order_items`;
CREATE TABLE `order_items`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `dish_id` bigint NOT NULL COMMENT '菜品ID',
  `dish_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜品名称',
  `dish_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜品图片',
  `price` decimal(10, 2) NOT NULL COMMENT '单价',
  `quantity` int NOT NULL COMMENT '数量',
  `subtotal` decimal(10, 2) NOT NULL COMMENT '小计',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_dish_id`(`dish_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_items
-- ----------------------------
INSERT INTO `order_items` VALUES (1, 1, 7, '22222', 'https://food-menu-yao.oss-cn-shenzhen.aliyuncs.com/food-menu/2025-11-22/82f3058e-4e35-4d78-92ab-ff09d7d4f2a4-%E5%B1%8F%E5%B9%95%E6%88%AA%E5%9B%BE%284%29.png?Expires=1763895931&OSSAccessKeyId=LTAI5tMRyKmbt7EZRgXjJRTk&Signature=YK6Y6YKpPztsp6A7%2B9AP6Sya7ig%3D', 22.00, 1, 22.00, '2025-11-22 19:05:36');
INSERT INTO `order_items` VALUES (2, 2, 2, '麻婆豆腐', 'https://dummyimage.com/800x600/0f172a/ffffff&text=family+dish', 28.00, 1, 28.00, '2025-11-22 19:40:46');
INSERT INTO `order_items` VALUES (3, 3, 7, '22222', 'https://food-menu-yao.oss-cn-shenzhen.aliyuncs.com/food-menu/2025-11-22/82f3058e-4e35-4d78-92ab-ff09d7d4f2a4-%E5%B1%8F%E5%B9%95%E6%88%AA%E5%9B%BE%284%29.png?Expires=1763898076&OSSAccessKeyId=LTAI5tMRyKmbt7EZRgXjJRTk&Signature=%2BVLsKm%2Bmxjxoyd8QAO3vuDDWh%2Bk%3D', 22.00, 1, 22.00, '2025-11-22 19:41:25');
INSERT INTO `order_items` VALUES (4, 4, 2, '麻婆豆腐', 'https://dummyimage.com/800x600/0f172a/ffffff&text=family+dish', 28.00, 1, 28.00, '2025-11-22 20:45:15');
INSERT INTO `order_items` VALUES (5, 4, 5, '番茄炒蛋', 'https://dummyimage.com/800x600/0f172a/ffffff&text=family+dish', 18.00, 1, 18.00, '2025-11-22 20:45:15');
INSERT INTO `order_items` VALUES (6, 4, 6, '米饭', 'https://dummyimage.com/800x600/0f172a/ffffff&text=family+dish', 2.00, 1, 2.00, '2025-11-22 20:45:15');
INSERT INTO `order_items` VALUES (7, 4, 7, '22222', 'https://food-menu-yao.oss-cn-shenzhen.aliyuncs.com/food-menu/2025-11-22/82f3058e-4e35-4d78-92ab-ff09d7d4f2a4-%E5%B1%8F%E5%B9%95%E6%88%AA%E5%9B%BE%284%29.png?Expires=1763901882&OSSAccessKeyId=LTAI5tMRyKmbt7EZRgXjJRTk&Signature=oQAZQZFsEqm9%2FcFCYkuuTum5tUI%3D', 22.00, 1, 22.00, '2025-11-22 20:45:15');
INSERT INTO `order_items` VALUES (8, 5, 2, '麻婆豆腐', 'https://dummyimage.com/800x600/0f172a/ffffff&text=family+dish', 28.00, 2, 56.00, '2025-11-22 23:43:35');
INSERT INTO `order_items` VALUES (9, 5, 4, '白切鸡', 'https://dummyimage.com/800x600/0f172a/ffffff&text=family+dish', 48.00, 1, 48.00, '2025-11-22 23:43:35');
INSERT INTO `order_items` VALUES (10, 5, 6, '米饭', 'https://dummyimage.com/800x600/0f172a/ffffff&text=family+dish', 2.00, 1, 2.00, '2025-11-22 23:43:35');
INSERT INTO `order_items` VALUES (11, 6, 2, '麻婆豆腐', 'https://dummyimage.com/800x600/0f172a/ffffff&text=family+dish', 28.00, 1, 28.00, '2025-11-22 23:46:08');
INSERT INTO `order_items` VALUES (12, 6, 5, '番茄炒蛋', 'https://dummyimage.com/800x600/0f172a/ffffff&text=family+dish', 18.00, 1, 18.00, '2025-11-22 23:46:08');
INSERT INTO `order_items` VALUES (13, 6, 6, '米饭', 'https://dummyimage.com/800x600/0f172a/ffffff&text=family+dish', 2.00, 2, 4.00, '2025-11-22 23:46:08');
INSERT INTO `order_items` VALUES (14, 6, 7, '22222', 'https://food-menu-yao.oss-cn-shenzhen.aliyuncs.com/food-menu/2025-11-22/82f3058e-4e35-4d78-92ab-ff09d7d4f2a4-%E5%B1%8F%E5%B9%95%E6%88%AA%E5%9B%BE%284%29.png?Expires=1763912732&OSSAccessKeyId=LTAI5tMRyKmbt7EZRgXjJRTk&Signature=TS4LbZ15tAwPPk9MKgitm0I4GRs%3D', 22.00, 1, 22.00, '2025-11-22 23:46:08');

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_number` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单编号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '订单状态 0-待接单 1-准备中 2-配送中 3-已完成 4-已取消',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '订单备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_number`(`order_number` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (1, 'FM202511221905360001', 2, 22.00, 3, '', '2025-11-22 19:05:36', '2025-11-22 19:05:36');
INSERT INTO `orders` VALUES (2, 'FM202511221940460001', 2, 28.00, 4, '谢谢', '2025-11-22 19:40:46', '2025-11-22 19:40:46');
INSERT INTO `orders` VALUES (3, 'FM202511221941250002', 2, 22.00, 4, '', '2025-11-22 19:41:25', '2025-11-22 19:41:25');
INSERT INTO `orders` VALUES (4, 'FM202511222045150003', 2, 70.00, 1, '靠靠靠靠靠靠靠', '2025-11-22 20:45:15', '2025-11-22 20:45:15');
INSERT INTO `orders` VALUES (5, 'FM202511222343340001', 1, 106.00, 3, '快快快', '2025-11-22 23:43:35', '2025-11-22 23:43:35');
INSERT INTO `orders` VALUES (6, 'FM202511222346080002', 1, 72.00, 0, '酷酷酷酷酷', '2025-11-22 23:46:08', '2025-11-22 23:46:08');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鐢ㄦ埛鍚',
  `password` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '瀵嗙爜(鍔犲瘑)',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鎵嬫満鍙',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '濮撳悕',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '澶村儚',
  `status` int NULL DEFAULT 1 COMMENT '鐘舵? 0:绂佺敤 1:姝ｅ父',
  `create_time` datetime NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `idx_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐢ㄦ埛琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13800138000', '管理员', NULL, 1, '2025-11-21 14:48:37', '2025-11-21 14:48:37');
INSERT INTO `user` VALUES (2, 'test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13800138001', '测试用户', NULL, 1, '2025-11-21 14:48:37', '2025-11-21 14:48:37');
INSERT INTO `user` VALUES (3, 'long', '$2a$10$ej.O.Bd2Wcz4J4TCTTxX5..TqFze.IJV2nDiX4arM/kVAI57KKeqC', '18320636653', 'long', NULL, 1, '2025-11-23 17:44:20', '2025-11-23 17:44:20');

-- ----------------------------
-- Table structure for wx_user
-- ----------------------------
DROP TABLE IF EXISTS `wx_user`;
CREATE TABLE `wx_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `openid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信OpenID',
  `unionid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信UnionID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `gender` int NULL DEFAULT 0 COMMENT '性别 0:未知 1:男 2:女',
  `status` int NULL DEFAULT 1 COMMENT '状态 0:禁用 1:正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_openid`(`openid` ASC) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '微信小程序用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wx_user
-- ----------------------------
INSERT INTO `wx_user` VALUES (1, NULL, NULL, 'testuser', '$2a$10$hVxizz1IIYgR6kfd93omPuFjWpkEMcf5skkXEupGG5zcUSPoF0Xh.', '13900139000', '哈哈哈', 'avatars/2025-11-22/155919aa-8195-4683-9ad4-c1c0261a3642-7D5hSM1YhD4Ca71f393efdb2c9f128fbea2335fc248b.jpg', 0, 1, '2025-11-22 16:13:38', '2025-11-23 00:49:09', 0);
INSERT INTO `wx_user` VALUES (2, NULL, NULL, '谢谢', '$2a$10$A0L9e3OMOKtTWflA8AfjzeowzbyOpb3mWT.eT3vTWDYv9/uAY7Tpi', '', '大灰狼', 'avatars/2025-11-22/81207505-f172-4b04-9d05-7b474914cd3a-ahC04MiN6Ddpe86ac75c94006bd7a1cd2f069633a210.jpg', 0, 1, '2025-11-22 17:51:41', '2025-11-22 17:52:00', 0);
INSERT INTO `wx_user` VALUES (3, NULL, NULL, 'long', '$2a$10$epnCwD.0n2xXz17Q3iv.6OYPrXnpoqpt00H/bKOyqBZ9odtn3lzui', NULL, '用户_long', NULL, 0, 1, '2025-11-23 17:30:49', '2025-11-23 17:30:49', 0);

SET FOREIGN_KEY_CHECKS = 1;
