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

 Date: 30/11/2025 01:03:05
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
  `local_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '本地图片路径',
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
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '轮播图表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of banner
-- ----------------------------
INSERT INTO `banner` VALUES (1, 'food-menu/2025-11-27/a0ba6fd4-a1b6-4953-a775-9d4766060117-pexels-gu-ko-2150570603-31972203.jpg', 'food-menu/a0ba6fd4-a1b6-4953-a775-9d4766060117-pexels-gu-ko-2150570603-31972203.jpg', '家庭美食精选', '每日新鲜食材，用心烹饪每一道菜', '/pages/menu/menu', 1, 1, '2025-11-22 03:13:28', '2025-11-27 14:39:23', 1, NULL, 0, 1);
INSERT INTO `banner` VALUES (2, 'food-menu/2025-11-27/9dc01787-f681-4c40-a2a7-1c5501a193de-pexels-gu-ko-2150570603-31987439.jpg', 'food-menu/2025-11-27/9dc01787-f681-4c40-a2a7-1c5501a193de-pexels-gu-ko-2150570603-31987439.jpg', '温馨家宴', '与家人共享美好时光', '/pages/menu/menu', 2, 1, '2025-11-22 03:13:28', '2025-11-27 21:22:10', 1, NULL, 0, 1);
INSERT INTO `banner` VALUES (3, 'food-menu/2025-11-27/d8c29cfc-e5f3-465b-bea9-d5dcb75e407e-pexels-valeriya-1639565.jpg', 'food-menu/2025-11-27/d8c29cfc-e5f3-465b-bea9-d5dcb75e407e-pexels-valeriya-1639565.jpg', '健康饮食', '营养搭配，健康每一天', '/pages/menu/menu', 3, 1, '2025-11-22 03:13:28', '2025-11-27 21:22:19', 1, NULL, 0, 1);
INSERT INTO `banner` VALUES (4, 'food-menu/2025-11-27/81224b65-4ba6-4854-be18-927c06778adb-pexels-kyleroxas-2122294.jpg', 'food-menu/81224b65-4ba6-4854-be18-927c06778adb-pexels-kyleroxas-2122294.jpg', '轻食', '22', NULL, 4, 1, '2025-11-22 17:27:07', '2025-11-27 14:34:27', NULL, NULL, 0, 1);

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
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_category_name`(`name` ASC) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜品分类' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, 1, '热菜', 1, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 1);
INSERT INTO `category` VALUES (2, 1, '凉菜', 2, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 1);
INSERT INTO `category` VALUES (3, 1, '汤羹', 3, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 1);
INSERT INTO `category` VALUES (4, 1, '主食', 4, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 1);
INSERT INTO `category` VALUES (5, 1, '素菜', 5, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 1);
INSERT INTO `category` VALUES (6, 1, '海鲜', 6, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 1);
INSERT INTO `category` VALUES (7, 1, '烧烤', 7, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 1);
INSERT INTO `category` VALUES (8, 1, '甜品', 8, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 1);

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
  `local_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '本地图片路径',
  `local_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '所有图片路径JSON数组',
  `description` varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鎻忚堪淇℃伅',
  `status` int NOT NULL DEFAULT 1 COMMENT '0:鍋滃敭 1:璧峰敭',
  `sort` int NOT NULL DEFAULT 0 COMMENT '椤哄簭',
  `create_time` datetime NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `create_user` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜',
  `update_user` bigint NULL DEFAULT NULL COMMENT '淇?敼浜',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '鏄?惁鍒犻櫎',
  `calories` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '卡路里',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签',
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 63 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜品' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dish
-- ----------------------------
INSERT INTO `dish` VALUES (9, '宫保鸡丁', 1, 38.00, 'RC001', '', 'https://dummyimage.com/600x400/ff6b6b/ffffff&text=宫保鸡丁', '[\"https://dummyimage.com/600x400/ff6b6b/ffffff&text=宫保鸡丁\"]', '经典川菜，鸡肉鲜嫩，花生香脆，酸甜微辣', 1, 1, '2025-11-29 01:09:42', '2025-11-29 01:17:08', 1, NULL, 0, '320千卡', '川菜,辣,下饭', 1);
INSERT INTO `dish` VALUES (10, '鱼香肉丝', 1, 32.00, 'RC002', '', 'https://dummyimage.com/600x400/ff8787/ffffff&text=鱼香肉丝', '[\"https://dummyimage.com/600x400/ff8787/ffffff&text=鱼香肉丝\"]', '肉丝滑嫩，酱汁浓郁，鱼香味十足', 1, 2, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '280千卡', '川菜,酸甜,家常', 1);
INSERT INTO `dish` VALUES (11, '红烧肉', 1, 48.00, 'RC003', '', 'https://dummyimage.com/600x400/c92a2a/ffffff&text=红烧肉', '[\"https://dummyimage.com/600x400/c92a2a/ffffff&text=红烧肉\"]', '肥而不腻，入口即化，色泽红亮', 1, 3, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '450千卡', '本帮菜,甜,经典', 1);
INSERT INTO `dish` VALUES (12, '麻婆豆腐', 1, 28.00, 'RC004', '', 'https://dummyimage.com/600x400/fa5252/ffffff&text=麻婆豆腐', '[\"https://dummyimage.com/600x400/fa5252/ffffff&text=麻婆豆腐\"]', '麻辣鲜香，豆腐嫩滑，下饭神器', 1, 4, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '220千卡', '川菜,麻辣,素食', 1);
INSERT INTO `dish` VALUES (13, '糖醋里脊', 1, 42.00, 'RC005', '', 'https://dummyimage.com/600x400/ff6b6b/ffffff&text=糖醋里脊', '[\"https://dummyimage.com/600x400/ff6b6b/ffffff&text=糖醋里脊\"]', '外酥里嫩，酸甜可口，色泽金黄', 1, 5, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '380千卡', '鲁菜,酸甜,儿童最爱', 1);
INSERT INTO `dish` VALUES (14, '水煮鱼', 1, 68.00, 'RC006', '', 'https://dummyimage.com/600x400/e03131/ffffff&text=水煮鱼', '[\"https://dummyimage.com/600x400/e03131/ffffff&text=水煮鱼\"]', '鱼肉鲜嫩，麻辣鲜香，汤汁浓郁', 1, 6, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '420千卡', '川菜,麻辣,招牌', 1);
INSERT INTO `dish` VALUES (15, '回锅肉', 1, 45.00, 'RC007', '', 'https://dummyimage.com/600x400/f03e3e/ffffff&text=回锅肉', '[\"https://dummyimage.com/600x400/f03e3e/ffffff&text=回锅肉\"]', '肥瘦相间，香辣下饭，川菜经典', 1, 7, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '480千卡', '川菜,辣,经典', 1);
INSERT INTO `dish` VALUES (16, '东坡肉', 1, 58.00, 'RC008', 'http://localhost:8080/uploads/https://dummyimage.com/600x400/c92a2a/ffffff&text=东坡肉', 'https://dummyimage.com/600x400/c92a2a/ffffff&text=东坡肉', '[\"https://dummyimage.com/600x400/c92a2a/ffffff&text=东坡肉\",\"food-menu/2025-11-29/b75a1b5c-15bc-40b1-b303-53f4bc81ba1b-pexels-gu-ko-2150570603-31972203.jpg\",\"food-menu/2025-11-29/31c30804-d084-4994-be86-dc368ae9d775-pexels-valeriya-1639565.jpg\"]', '酥烂而形不碎，香糯而不腻口', 1, 8, '2025-11-29 01:09:42', '2025-11-29 03:25:00', 1, NULL, 0, '520千卡', '甜,特色', 1);
INSERT INTO `dish` VALUES (17, '小炒肉', 1, 35.00, 'RC009', '', 'https://dummyimage.com/600x400/ff6b6b/ffffff&text=小炒肉', '[\"https://dummyimage.com/600x400/ff6b6b/ffffff&text=小炒肉\"]', '湘菜经典，肉片嫩滑，辣椒香脆', 1, 9, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '340千卡', '湘菜,辣,家常', 1);
INSERT INTO `dish` VALUES (18, '青椒肉丝', 1, 30.00, 'RC010', '', 'https://dummyimage.com/600x400/ff8787/ffffff&text=青椒肉丝', '[\"https://dummyimage.com/600x400/ff8787/ffffff&text=青椒肉丝\"]', '家常小炒，简单美味，营养均衡', 1, 10, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '260千卡', '家常菜,微辣,健康', 1);
INSERT INTO `dish` VALUES (19, '夫妻肺片', 2, 36.00, 'LC001', '', 'https://dummyimage.com/600x400/51cf66/ffffff&text=夫妻肺片', '[\"https://dummyimage.com/600x400/51cf66/ffffff&text=夫妻肺片\"]', '麻辣鲜香，色泽红亮，质地软嫩', 1, 1, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '280千卡', '川菜,麻辣,凉菜', 1);
INSERT INTO `dish` VALUES (20, '口水鸡', 2, 38.00, 'LC002', '', 'https://dummyimage.com/600x400/69db7c/ffffff&text=口水鸡', '[\"https://dummyimage.com/600x400/69db7c/ffffff&text=口水鸡\"]', '鸡肉鲜嫩，麻辣鲜香，让人垂涎', 1, 2, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '240千卡', '川菜,麻辣,开胃', 1);
INSERT INTO `dish` VALUES (21, '凉拌黄瓜', 2, 18.00, 'LC003', '', 'https://dummyimage.com/600x400/8ce99a/ffffff&text=凉拌黄瓜', '[\"https://dummyimage.com/600x400/8ce99a/ffffff&text=凉拌黄瓜\"]', '清爽脆嫩，酸甜可口，解腻开胃', 1, 3, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '45千卡', '素食,清淡,健康', 1);
INSERT INTO `dish` VALUES (22, '皮蛋豆腐', 2, 22.00, 'LC004', '', 'https://dummyimage.com/600x400/b2f2bb/ffffff&text=皮蛋豆腐', '[\"https://dummyimage.com/600x400/b2f2bb/ffffff&text=皮蛋豆腐\"]', '豆腐嫩滑，皮蛋香醇，清爽可口', 1, 4, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '180千卡', '家常菜,清淡,营养', 1);
INSERT INTO `dish` VALUES (23, '拍黄瓜', 2, 16.00, 'LC005', '', 'https://dummyimage.com/600x400/8ce99a/ffffff&text=拍黄瓜', '[\"https://dummyimage.com/600x400/8ce99a/ffffff&text=拍黄瓜\"]', '简单美味，蒜香浓郁，清脆爽口', 1, 5, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '40千卡', '素食,清淡,开胃', 1);
INSERT INTO `dish` VALUES (24, '凉拌木耳', 2, 20.00, 'LC006', '', 'https://dummyimage.com/600x400/69db7c/ffffff&text=凉拌木耳', '[\"https://dummyimage.com/600x400/69db7c/ffffff&text=凉拌木耳\"]', '口感脆爽，营养丰富，清淡健康', 1, 6, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '55千卡', '素食,健康,清淡', 1);
INSERT INTO `dish` VALUES (25, '蒜泥白肉', 2, 42.00, 'LC007', '', 'https://dummyimage.com/600x400/51cf66/ffffff&text=蒜泥白肉', '[\"https://dummyimage.com/600x400/51cf66/ffffff&text=蒜泥白肉\"]', '肉片薄而不散，蒜香浓郁，肥而不腻', 1, 7, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '320千卡', '川菜,蒜香,经典', 1);
INSERT INTO `dish` VALUES (26, '西湖牛肉羹', 3, 32.00, 'TG001', '', 'https://dummyimage.com/600x400/ffd43b/ffffff&text=西湖牛肉羹', '[\"https://dummyimage.com/600x400/ffd43b/ffffff&text=西湖牛肉羹\"]', '汤汁浓稠，牛肉鲜嫩，营养丰富', 1, 1, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '180千卡', '浙菜,营养,暖胃', 1);
INSERT INTO `dish` VALUES (27, '酸辣汤', 3, 22.00, 'TG002', '', 'https://dummyimage.com/600x400/fab005/ffffff&text=酸辣汤', '[\"https://dummyimage.com/600x400/fab005/ffffff&text=酸辣汤\"]', '酸辣开胃，汤汁浓郁，暖胃驱寒', 1, 2, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '120千卡', '川菜,酸辣,开胃', 1);
INSERT INTO `dish` VALUES (28, '番茄蛋花汤', 3, 18.00, 'TG003', '', 'https://dummyimage.com/600x400/ffe066/ffffff&text=番茄蛋花汤', '[\"https://dummyimage.com/600x400/ffe066/ffffff&text=番茄蛋花汤\"]', '酸甜可口，营养丰富，老少皆宜', 1, 3, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '90千卡', '家常菜,营养,清淡', 1);
INSERT INTO `dish` VALUES (29, '紫菜蛋花汤', 3, 16.00, 'TG004', '', 'https://dummyimage.com/600x400/ffd43b/ffffff&text=紫菜蛋花汤', '[\"https://dummyimage.com/600x400/ffd43b/ffffff&text=紫菜蛋花汤\"]', '清淡鲜美，营养丰富，简单美味', 1, 4, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '75千卡', '家常菜,清淡,营养', 1);
INSERT INTO `dish` VALUES (30, '玉米排骨汤', 3, 38.00, 'TG005', '', 'https://dummyimage.com/600x400/fab005/ffffff&text=玉米排骨汤', '[\"https://dummyimage.com/600x400/fab005/ffffff&text=玉米排骨汤\"]', '排骨酥烂，玉米香甜，汤汁鲜美', 1, 5, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '220千卡', '粤菜,营养,滋补', 1);
INSERT INTO `dish` VALUES (31, '冬瓜排骨汤', 3, 35.00, 'TG006', '', 'https://dummyimage.com/600x400/ffe066/ffffff&text=冬瓜排骨汤', '[\"https://dummyimage.com/600x400/ffe066/ffffff&text=冬瓜排骨汤\"]', '清热解暑，营养滋补，汤汁清甜', 1, 6, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '180千卡', '粤菜,清淡,养生', 1);
INSERT INTO `dish` VALUES (32, '扬州炒饭', 4, 28.00, 'ZS001', '', 'https://dummyimage.com/600x400/74c0fc/ffffff&text=扬州炒饭', '[\"https://dummyimage.com/600x400/74c0fc/ffffff&text=扬州炒饭\"]', '粒粒分明，配料丰富，色香味俱全', 1, 1, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '480千卡', '主食,炒饭,经典', 1);
INSERT INTO `dish` VALUES (33, '蛋炒饭', 4, 18.00, 'ZS002', '', 'https://dummyimage.com/600x400/4dabf7/ffffff&text=蛋炒饭', '[\"https://dummyimage.com/600x400/4dabf7/ffffff&text=蛋炒饭\"]', '简单美味，蛋香浓郁，家常必备', 1, 2, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '420千卡', '主食,炒饭,家常', 1);
INSERT INTO `dish` VALUES (34, '牛肉面', 4, 32.00, 'ZS003', '', 'https://dummyimage.com/600x400/339af0/ffffff&text=牛肉面', '[\"https://dummyimage.com/600x400/339af0/ffffff&text=牛肉面\"]', '牛肉酥烂，汤汁浓郁，面条劲道', 1, 3, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '520千卡', '主食,面食,营养', 1);
INSERT INTO `dish` VALUES (35, '小笼包', 4, 25.00, 'ZS004', '', 'https://dummyimage.com/600x400/74c0fc/ffffff&text=小笼包', '[\"https://dummyimage.com/600x400/74c0fc/ffffff&text=小笼包\"]', '皮薄馅大，汤汁鲜美，江南名点', 1, 4, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '280千卡', '主食,点心,特色', 1);
INSERT INTO `dish` VALUES (36, '煎饺', 4, 22.00, 'ZS005', '', 'https://dummyimage.com/600x400/4dabf7/ffffff&text=煎饺', '[\"https://dummyimage.com/600x400/4dabf7/ffffff&text=煎饺\"]', '底部金黄酥脆，馅料鲜美多汁', 1, 5, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '320千卡', '主食,点心,香脆', 1);
INSERT INTO `dish` VALUES (37, '葱油拌面', 4, 20.00, 'ZS006', '', 'https://dummyimage.com/600x400/339af0/ffffff&text=葱油拌面', '[\"https://dummyimage.com/600x400/339af0/ffffff&text=葱油拌面\"]', '葱香浓郁，面条爽滑，简单美味', 1, 6, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '380千卡', '主食,面食,清淡', 1);
INSERT INTO `dish` VALUES (38, '馄饨', 4, 24.00, 'ZS007', '', 'https://dummyimage.com/600x400/74c0fc/ffffff&text=馄饨', '[\"https://dummyimage.com/600x400/74c0fc/ffffff&text=馄饨\"]', '皮薄馅嫩，汤汁鲜美，温暖贴心', 1, 7, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '260千卡', '主食,点心,营养', 1);
INSERT INTO `dish` VALUES (39, '地三鲜', 5, 28.00, 'SC001', '', 'https://dummyimage.com/600x400/94d82d/ffffff&text=地三鲜', '[\"https://dummyimage.com/600x400/94d82d/ffffff&text=地三鲜\"]', '茄子、土豆、青椒，东北名菜', 1, 1, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '320千卡', '东北菜,素食,下饭', 1);
INSERT INTO `dish` VALUES (40, '干煸豆角', 5, 26.00, 'SC002', '', 'https://dummyimage.com/600x400/a9e34b/ffffff&text=干煸豆角', '[\"https://dummyimage.com/600x400/a9e34b/ffffff&text=干煸豆角\"]', '豆角干香，口感脆嫩，川菜经典', 1, 2, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '180千卡', '川菜,素食,香脆', 1);
INSERT INTO `dish` VALUES (41, '清炒时蔬', 5, 22.00, 'SC003', '', 'https://dummyimage.com/600x400/c0eb75/ffffff&text=清炒时蔬', '[\"https://dummyimage.com/600x400/c0eb75/ffffff&text=清炒时蔬\"]', '新鲜时令蔬菜，清淡健康，营养丰富', 1, 3, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '85千卡', '素食,清淡,健康', 1);
INSERT INTO `dish` VALUES (42, '蒜蓉西兰花', 5, 24.00, 'SC004', '', 'https://dummyimage.com/600x400/d8f5a2/ffffff&text=蒜蓉西兰花', '[\"https://dummyimage.com/600x400/d8f5a2/ffffff&text=蒜蓉西兰花\"]', '西兰花翠绿，蒜香浓郁，营养丰富', 1, 4, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '95千卡', '素食,健康,蒜香', 1);
INSERT INTO `dish` VALUES (43, '手撕包菜', 5, 20.00, 'SC005', '', 'https://dummyimage.com/600x400/94d82d/ffffff&text=手撕包菜', '[\"https://dummyimage.com/600x400/94d82d/ffffff&text=手撕包菜\"]', '包菜脆嫩，酸辣开胃，简单美味', 1, 5, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '75千卡', '素食,酸辣,开胃', 1);
INSERT INTO `dish` VALUES (44, '炒土豆丝', 5, 18.00, 'SC006', '', 'https://dummyimage.com/600x400/a9e34b/ffffff&text=炒土豆丝', '[\"https://dummyimage.com/600x400/a9e34b/ffffff&text=炒土豆丝\"]', '土豆丝脆爽，酸辣可口，家常必备', 1, 6, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '120千卡', '素食,家常,清淡', 1);
INSERT INTO `dish` VALUES (45, '蚝油生菜', 5, 22.00, 'SC007', '', 'https://dummyimage.com/600x400/c0eb75/ffffff&text=蚝油生菜', '[\"https://dummyimage.com/600x400/c0eb75/ffffff&text=蚝油生菜\"]', '生菜翠绿，蚝油鲜香，清淡爽口', 1, 7, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '65千卡', '素食,清淡,鲜香', 1);
INSERT INTO `dish` VALUES (46, '清蒸鲈鱼', 6, 88.00, 'HX001', '', 'https://dummyimage.com/600x400/3bc9db/ffffff&text=清蒸鲈鱼', '[\"https://dummyimage.com/600x400/3bc9db/ffffff&text=清蒸鲈鱼\"]', '鱼肉鲜嫩，原汁原味，营养丰富', 1, 1, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '180千卡', '粤菜,海鲜,清淡', 1);
INSERT INTO `dish` VALUES (47, '红烧大虾', 6, 98.00, 'HX002', '', 'https://dummyimage.com/600x400/22b8cf/ffffff&text=红烧大虾', '[\"https://dummyimage.com/600x400/22b8cf/ffffff&text=红烧大虾\"]', '虾肉Q弹，酱汁浓郁，色泽红亮', 1, 2, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '220千卡', '鲁菜,海鲜,鲜美', 1);
INSERT INTO `dish` VALUES (48, '蒜蓉粉丝蒸扇贝', 6, 78.00, 'HX003', '', 'https://dummyimage.com/600x400/15aabf/ffffff&text=蒜蓉粉丝蒸扇贝', '[\"https://dummyimage.com/600x400/15aabf/ffffff&text=蒜蓉粉丝蒸扇贝\"]', '扇贝鲜美，蒜香浓郁，粉丝爽滑', 1, 3, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '160千卡', '粤菜,海鲜,蒜香', 1);
INSERT INTO `dish` VALUES (49, '椒盐皮皮虾', 6, 68.00, 'HX004', '', 'https://dummyimage.com/600x400/3bc9db/ffffff&text=椒盐皮皮虾', '[\"https://dummyimage.com/600x400/3bc9db/ffffff&text=椒盐皮皮虾\"]', '外酥里嫩，椒盐香脆，鲜香可口', 1, 4, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '240千卡', '粤菜,海鲜,香脆', 1);
INSERT INTO `dish` VALUES (50, '爆炒鱿鱼', 6, 58.00, 'HX005', '', 'https://dummyimage.com/600x400/22b8cf/ffffff&text=爆炒鱿鱼', '[\"https://dummyimage.com/600x400/22b8cf/ffffff&text=爆炒鱿鱼\"]', '鱿鱼爽脆，酱汁浓郁，鲜香美味', 1, 5, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '200千卡', '川菜,海鲜,鲜辣', 1);
INSERT INTO `dish` VALUES (51, '蒜蓉开背虾', 6, 108.00, 'HX006', '', 'https://dummyimage.com/600x400/15aabf/ffffff&text=蒜蓉开背虾', '[\"https://dummyimage.com/600x400/15aabf/ffffff&text=蒜蓉开背虾\"]', '虾肉饱满，蒜香浓郁，鲜美诱人', 1, 6, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '260千卡', '粤菜,海鲜,蒜香', 1);
INSERT INTO `dish` VALUES (52, '烤羊肉串', 7, 38.00, 'SK001', '', 'https://dummyimage.com/600x400/f59f00/ffffff&text=烤羊肉串', '[\"https://dummyimage.com/600x400/f59f00/ffffff&text=烤羊肉串\"]', '肉质鲜嫩，孜然香浓，新疆风味', 1, 1, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '280千卡', '烧烤,羊肉,香辣', 1);
INSERT INTO `dish` VALUES (53, '烤鸡翅', 7, 32.00, 'SK002', '', 'https://dummyimage.com/600x400/f76707/ffffff&text=烤鸡翅', '[\"https://dummyimage.com/600x400/f76707/ffffff&text=烤鸡翅\"]', '外焦里嫩，香气扑鼻，人气单品', 1, 2, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '240千卡', '烧烤,鸡肉,香脆', 1);
INSERT INTO `dish` VALUES (54, '烤茄子', 7, 18.00, 'SK003', '', 'https://dummyimage.com/600x400/fd7e14/ffffff&text=烤茄子', '[\"https://dummyimage.com/600x400/fd7e14/ffffff&text=烤茄子\"]', '茄子软烂，蒜香浓郁，素食之选', 1, 3, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '120千卡', '烧烤,素食,蒜香', 1);
INSERT INTO `dish` VALUES (55, '烤韭菜', 7, 15.00, 'SK004', '', 'https://dummyimage.com/600x400/f59f00/ffffff&text=烤韭菜', '[\"https://dummyimage.com/600x400/f59f00/ffffff&text=烤韭菜\"]', '韭菜鲜嫩，香气四溢，开胃下酒', 1, 4, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '65千卡', '烧烤,素食,清香', 1);
INSERT INTO `dish` VALUES (56, '烤五花肉', 7, 42.00, 'SK005', '', 'https://dummyimage.com/600x400/f76707/ffffff&text=烤五花肉', '[\"https://dummyimage.com/600x400/f76707/ffffff&text=烤五花肉\"]', '肥瘦相间，香脆可口，肉香浓郁', 1, 5, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '420千卡', '烧烤,猪肉,香脆', 1);
INSERT INTO `dish` VALUES (57, '红豆双皮奶', 8, 22.00, 'TP001', '', 'https://dummyimage.com/600x400/e599f7/ffffff&text=红豆双皮奶', '[\"https://dummyimage.com/600x400/e599f7/ffffff&text=红豆双皮奶\"]', '奶香浓郁，口感细腻，甜而不腻', 1, 1, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '280千卡', '甜品,奶制品,经典', 1);
INSERT INTO `dish` VALUES (58, '芒果班戟', 8, 28.00, 'TP002', '', 'https://dummyimage.com/600x400/da77f2/ffffff&text=芒果班戟', '[\"https://dummyimage.com/600x400/da77f2/ffffff&text=芒果班戟\"]', '芒果香甜，奶油细腻，清新可口', 1, 2, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '320千卡', '甜品,水果,清新', 1);
INSERT INTO `dish` VALUES (59, '杨枝甘露', 8, 26.00, 'TP003', '', 'https://dummyimage.com/600x400/cc5de8/ffffff&text=杨枝甘露', '[\"https://dummyimage.com/600x400/cc5de8/ffffff&text=杨枝甘露\"]', '芒果西柚，清爽甜蜜，港式经典', 1, 3, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '240千卡', '甜品,水果,清爽', 1);
INSERT INTO `dish` VALUES (60, '提拉米苏', 8, 32.00, 'TP004', '', 'https://dummyimage.com/600x400/e599f7/ffffff&text=提拉米苏', '[\"https://dummyimage.com/600x400/e599f7/ffffff&text=提拉米苏\"]', '咖啡香浓，芝士细腻，意式经典', 1, 4, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '380千卡', '甜品,咖啡,浓郁', 1);
INSERT INTO `dish` VALUES (61, '冰糖雪梨', 8, 18.00, 'TP005', '', 'https://dummyimage.com/600x400/da77f2/ffffff&text=冰糖雪梨', '[\"https://dummyimage.com/600x400/da77f2/ffffff&text=冰糖雪梨\"]', '清甜润肺，养生佳品，清热解暑', 1, 5, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '120千卡', '甜品,养生,清润', 1);
INSERT INTO `dish` VALUES (62, '龟苓膏', 8, 16.00, 'TP006', '', 'https://dummyimage.com/600x400/cc5de8/ffffff&text=龟苓膏', '[\"https://dummyimage.com/600x400/cc5de8/ffffff&text=龟苓膏\"]', '清热解毒，口感Q弹，养生甜品', 1, 6, '2025-11-29 01:09:42', '2025-11-29 01:09:42', 1, 1, 0, '95千卡', '甜品,养生,清热', 1);

-- ----------------------------
-- Table structure for dish_comment
-- ----------------------------
DROP TABLE IF EXISTS `dish_comment`;
CREATE TABLE `dish_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dish_id` bigint NOT NULL COMMENT '菜品id',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父评论ID',
  `wx_user_id` bigint NULL DEFAULT NULL COMMENT '微信用户ID',
  `author_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '展示昵称',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评论者头像URL',
  `reply_to_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '被回复人昵称',
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜品评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dish_comment
-- ----------------------------
INSERT INTO `dish_comment` VALUES (12, 7, NULL, 1, '哈哈哈', '666', '2025-11-26 18:29:06', 'avatars/2025-11-22/155919aa-8195-4683-9ad4-c1c0261a3642-7D5hSM1YhD4Ca71f393efdb2c9f128fbea2335fc248b.jpg', NULL, 1);
INSERT INTO `dish_comment` VALUES (13, 7, 12, 3, '用户_long', '222', '2025-11-26 18:29:23', 'avatars/2025-11-24/db9c2674-93d1-4508-8cf7-85e1683d798f-pexels-pixabay-89778.jpg', '哈哈哈', 1);
INSERT INTO `dish_comment` VALUES (14, 7, 12, 1, '哈哈哈', '333', '2025-11-26 18:29:36', 'avatars/2025-11-22/155919aa-8195-4683-9ad4-c1c0261a3642-7D5hSM1YhD4Ca71f393efdb2c9f128fbea2335fc248b.jpg', '用户_long', 1);
INSERT INTO `dish_comment` VALUES (15, 7, 12, 1, '哈哈哈', '深色', '2025-11-26 18:40:19', 'avatars/2025-11-22/155919aa-8195-4683-9ad4-c1c0261a3642-7D5hSM1YhD4Ca71f393efdb2c9f128fbea2335fc248b.jpg', '哈哈哈', 1);
INSERT INTO `dish_comment` VALUES (16, 7, 12, 1, '哈哈哈', '事实上', '2025-11-26 18:40:24', 'avatars/2025-11-22/155919aa-8195-4683-9ad4-c1c0261a3642-7D5hSM1YhD4Ca71f393efdb2c9f128fbea2335fc248b.jpg', '哈哈哈', 1);
INSERT INTO `dish_comment` VALUES (17, 7, 12, 1, '哈哈哈', '呜呜呜', '2025-11-26 18:40:33', 'avatars/2025-11-22/155919aa-8195-4683-9ad4-c1c0261a3642-7D5hSM1YhD4Ca71f393efdb2c9f128fbea2335fc248b.jpg', '用户_long', 1);
INSERT INTO `dish_comment` VALUES (18, 7, 12, 3, '用户_long', '日日日', '2025-11-26 18:41:03', 'avatars/2025-11-24/db9c2674-93d1-4508-8cf7-85e1683d798f-pexels-pixabay-89778.jpg', '用户_long', 1);
INSERT INTO `dish_comment` VALUES (19, 7, 12, 3, '用户_long', '让22', '2025-11-26 18:41:09', 'avatars/2025-11-24/db9c2674-93d1-4508-8cf7-85e1683d798f-pexels-pixabay-89778.jpg', '哈哈哈', 1);
INSERT INTO `dish_comment` VALUES (20, 7, 12, 1, '哈哈哈', '66666', '2025-11-26 18:57:41', 'avatars/2025-11-22/155919aa-8195-4683-9ad4-c1c0261a3642-7D5hSM1YhD4Ca71f393efdb2c9f128fbea2335fc248b.jpg', '用户_long', 1);
INSERT INTO `dish_comment` VALUES (21, 7, 12, 1, '哈哈哈', '5552', '2025-11-26 18:57:50', 'avatars/2025-11-22/155919aa-8195-4683-9ad4-c1c0261a3642-7D5hSM1YhD4Ca71f393efdb2c9f128fbea2335fc248b.jpg', '用户_long', 1);
INSERT INTO `dish_comment` VALUES (22, 7, 12, 1, '哈哈哈', '666', '2025-11-26 19:12:33', 'avatars/2025-11-22/155919aa-8195-4683-9ad4-c1c0261a3642-7D5hSM1YhD4Ca71f393efdb2c9f128fbea2335fc248b.jpg', '用户_long', 1);
INSERT INTO `dish_comment` VALUES (23, 7, 12, 1, '哈哈哈', '352', '2025-11-26 19:12:46', 'avatars/2025-11-22/155919aa-8195-4683-9ad4-c1c0261a3642-7D5hSM1YhD4Ca71f393efdb2c9f128fbea2335fc248b.jpg', '哈哈哈', 1);
INSERT INTO `dish_comment` VALUES (24, 7, 12, 1, '哈哈哈', '415641', '2025-11-26 19:12:54', 'avatars/2025-11-22/155919aa-8195-4683-9ad4-c1c0261a3642-7D5hSM1YhD4Ca71f393efdb2c9f128fbea2335fc248b.jpg', '用户_long', 1);
INSERT INTO `dish_comment` VALUES (25, 1, NULL, 1, '哈哈哈', '666', '2025-11-27 02:17:34', 'avatars/2025-11-22/155919aa-8195-4683-9ad4-c1c0261a3642-7D5hSM1YhD4Ca71f393efdb2c9f128fbea2335fc248b.jpg', NULL, 1);
INSERT INTO `dish_comment` VALUES (26, 16, NULL, 3, 'long', '水水水水', '2025-11-29 03:48:41', 'avatars/2025-11-28/0961a72c-8ee3-47d2-82d1-46d0a4aa62d1-okAZhN0b9lDNHCAeCsnaAG8QAxv7gAfIsvAAQA_tplv-dy-awe.png', NULL, 1);

-- ----------------------------
-- Table structure for dish_favorite
-- ----------------------------
DROP TABLE IF EXISTS `dish_favorite`;
CREATE TABLE `dish_favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `dish_id` bigint NOT NULL COMMENT '菜品ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '收藏时间',
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_dish`(`user_id` ASC, `dish_id` ASC) USING BTREE COMMENT '用户-菜品唯一索引',
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE COMMENT '用户ID索引',
  INDEX `idx_dish_id`(`dish_id` ASC) USING BTREE COMMENT '菜品ID索引',
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜品收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dish_favorite
-- ----------------------------
INSERT INTO `dish_favorite` VALUES (1, 2, 7, '2025-11-22 21:05:07', 1);
INSERT INTO `dish_favorite` VALUES (3, 2, 6, '2025-11-22 21:05:48', 1);
INSERT INTO `dish_favorite` VALUES (4, 1, 4, '2025-11-22 21:06:36', 1);
INSERT INTO `dish_favorite` VALUES (5, 1, 2, '2025-11-22 23:41:24', 1);
INSERT INTO `dish_favorite` VALUES (6, 1, 7, '2025-11-23 00:49:20', 1);
INSERT INTO `dish_favorite` VALUES (10, 3, 4, '2025-11-25 19:04:01', 1);
INSERT INTO `dish_favorite` VALUES (13, 3, 3, '2025-11-26 01:44:35', 1);
INSERT INTO `dish_favorite` VALUES (21, 3, 7, '2025-11-26 01:50:56', 1);
INSERT INTO `dish_favorite` VALUES (22, 1, 1, '2025-11-28 00:22:54', 1);
INSERT INTO `dish_favorite` VALUES (23, 1, 5, '2025-11-28 00:30:26', 1);
INSERT INTO `dish_favorite` VALUES (24, 1, 6, '2025-11-28 00:30:27', 1);
INSERT INTO `dish_favorite` VALUES (25, 3, 1, '2025-11-28 22:13:23', 1);
INSERT INTO `dish_favorite` VALUES (26, 3, 2, '2025-11-28 22:13:25', 1);
INSERT INTO `dish_favorite` VALUES (27, 3, 6, '2025-11-28 22:13:28', 1);
INSERT INTO `dish_favorite` VALUES (28, 3, 33, '2025-11-29 01:13:48', 1);
INSERT INTO `dish_favorite` VALUES (29, 3, 35, '2025-11-29 01:13:49', 1);
INSERT INTO `dish_favorite` VALUES (30, 3, 36, '2025-11-29 01:13:50', 1);
INSERT INTO `dish_favorite` VALUES (31, 3, 58, '2025-11-29 01:13:51', 1);
INSERT INTO `dish_favorite` VALUES (32, 3, 57, '2025-11-29 01:13:52', 1);
INSERT INTO `dish_favorite` VALUES (33, 3, 59, '2025-11-29 01:13:52', 1);
INSERT INTO `dish_favorite` VALUES (34, 3, 60, '2025-11-29 01:13:53', 1);
INSERT INTO `dish_favorite` VALUES (35, 3, 61, '2025-11-29 01:13:54', 1);
INSERT INTO `dish_favorite` VALUES (36, 3, 62, '2025-11-29 01:13:55', 1);
INSERT INTO `dish_favorite` VALUES (37, 3, 9, '2025-11-29 01:13:57', 1);
INSERT INTO `dish_favorite` VALUES (38, 3, 10, '2025-11-29 01:13:58', 1);
INSERT INTO `dish_favorite` VALUES (39, 3, 20, '2025-11-29 01:14:00', 1);
INSERT INTO `dish_favorite` VALUES (40, 3, 26, '2025-11-29 01:14:02', 1);
INSERT INTO `dish_favorite` VALUES (41, 3, 29, '2025-11-29 01:14:04', 1);

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
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鑿滃搧鍙ｅ懗琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dish_flavor
-- ----------------------------
INSERT INTO `dish_flavor` VALUES (19, 16, '口味', '[\"家常\"]', '2025-11-29 02:01:21', '2025-11-29 02:01:21', NULL, NULL, 1, 1);
INSERT INTO `dish_flavor` VALUES (20, 16, '口味', '[\"家常\"]', '2025-11-29 03:25:00', '2025-11-29 03:25:00', NULL, NULL, 0, 1);

-- ----------------------------
-- Table structure for dish_statistics
-- ----------------------------
DROP TABLE IF EXISTS `dish_statistics`;
CREATE TABLE `dish_statistics`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dish_id` bigint NOT NULL COMMENT '菜品ID',
  `total_order_count` int NOT NULL DEFAULT 0 COMMENT '总点单次数',
  `month_order_count` int NOT NULL DEFAULT 0 COMMENT '本月点单次数',
  `week_order_count` int NOT NULL DEFAULT 0 COMMENT '本周点单次数',
  `last_order_time` datetime NULL DEFAULT NULL COMMENT '最后点单时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dish_id`(`dish_id` ASC) USING BTREE COMMENT '菜品ID唯一索引',
  INDEX `idx_total_count`(`total_order_count` DESC) USING BTREE COMMENT '总次数索引',
  INDEX `idx_month_count`(`month_order_count` DESC) USING BTREE COMMENT '月度次数索引',
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 54 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜品统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dish_statistics
-- ----------------------------
INSERT INTO `dish_statistics` VALUES (18, 1, 2, 0, 0, '2025-11-28 16:48:15', '2025-11-28 16:48:15', '2025-11-29 22:27:15', 1);
INSERT INTO `dish_statistics` VALUES (22, 8, 4, 0, 0, '2025-11-28 16:48:20', '2025-11-28 16:48:15', '2025-11-29 22:27:15', 1);
INSERT INTO `dish_statistics` VALUES (36, 26, 2, 0, 0, '2025-11-29 15:31:54', '2025-11-29 01:47:18', '2025-11-29 22:27:15', 1);
INSERT INTO `dish_statistics` VALUES (37, 27, 1, 0, 0, '2025-11-29 01:15:50', '2025-11-29 01:47:18', '2025-11-29 22:27:15', 1);
INSERT INTO `dish_statistics` VALUES (38, 28, 1, 0, 0, '2025-11-29 01:15:50', '2025-11-29 01:47:18', '2025-11-29 22:27:15', 1);
INSERT INTO `dish_statistics` VALUES (39, 29, 1, 0, 0, '2025-11-29 01:15:50', '2025-11-29 01:47:18', '2025-11-29 22:27:15', 1);
INSERT INTO `dish_statistics` VALUES (45, 9, 3, 0, 0, '2025-11-29 15:31:54', '2025-11-29 15:29:17', '2025-11-29 22:27:15', 1);
INSERT INTO `dish_statistics` VALUES (46, 11, 2, 0, 0, '2025-11-29 15:31:04', '2025-11-29 15:29:17', '2025-11-29 22:27:15', 1);
INSERT INTO `dish_statistics` VALUES (50, 19, 1, 0, 0, '2025-11-29 15:31:54', '2025-11-29 15:31:54', '2025-11-29 22:27:15', 1);
INSERT INTO `dish_statistics` VALUES (52, 46, 1, 0, 0, '2025-11-29 15:31:54', '2025-11-29 15:31:54', '2025-11-29 22:27:15', 1);
INSERT INTO `dish_statistics` VALUES (53, 52, 1, 0, 0, '2025-11-29 15:31:54', '2025-11-29 15:31:54', '2025-11-29 22:27:15', 1);

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
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name` ASC) USING BTREE COMMENT '标签名称唯一索引',
  INDEX `idx_type`(`type` ASC) USING BTREE COMMENT '类型索引',
  INDEX `idx_status`(`status` ASC) USING BTREE COMMENT '状态索引',
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜品标签配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dish_tag
-- ----------------------------
INSERT INTO `dish_tag` VALUES (1, '川菜', '🌶️', 1, 1, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (2, '湘菜', '🔥', 1, 2, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (3, '粤菜', '🥘', 1, 3, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (4, '鲁菜', '🍖', 1, 4, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (5, '浙菜', '🦐', 1, 5, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (6, '东北菜', '🥟', 1, 6, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (7, '本帮菜', '🍲', 1, 7, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (8, '辣', '🌶️', 2, 1, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (9, '麻辣', '🔥', 2, 2, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (10, '酸辣', '🍋', 2, 3, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (11, '酸甜', '🍯', 2, 4, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (12, '甜', '🍬', 2, 5, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (13, '清淡', '🌿', 2, 6, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (14, '蒜香', '🧄', 2, 7, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (15, '鲜香', '✨', 2, 8, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (16, '香脆', '⚡', 2, 9, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (17, '下饭', '🍚', 3, 1, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (18, '家常', '🏠', 3, 2, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (19, '经典', '⭐', 3, 3, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (20, '招牌', '👑', 3, 4, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (21, '特色', '💎', 3, 5, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (22, '素食', '🥬', 3, 6, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (23, '健康', '💚', 3, 7, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (24, '营养', '🥗', 3, 8, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (25, '养生', '☘️', 3, 9, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (26, '儿童最爱', '👶', 3, 10, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (27, '开胃', '😋', 3, 11, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (28, '暖胃', '🫖', 3, 12, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (29, '滋补', '🍵', 3, 13, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (30, '海鲜', '🦞', 4, 1, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (31, '烧烤', '🍢', 4, 2, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (32, '主食', '🍜', 4, 3, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (33, '点心', '🥟', 4, 4, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);
INSERT INTO `dish_tag` VALUES (34, '甜品', '🍰', 4, 5, 1, '2025-11-29 01:39:44', '2025-11-29 01:39:44', 1, 1, 1);

-- ----------------------------
-- Table structure for family
-- ----------------------------
DROP TABLE IF EXISTS `family`;
CREATE TABLE `family`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '家庭名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '家庭描述',
  `invite_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邀请码',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_user` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `invite_code`(`invite_code` ASC) USING BTREE,
  UNIQUE INDEX `uk_invite_code`(`invite_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '家庭表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of family
-- ----------------------------
INSERT INTO `family` VALUES (1, '默认家庭', '系统默认家庭，用于迁移现有数据', 'DEFAULT001', 1, '2025-11-29 22:27:15', '2025-11-29 22:27:15', NULL, NULL);
INSERT INTO `family` VALUES (8, '啊哒哒', '啊哒哒啊哒哒啊哒哒啊哒哒啊哒哒啊哒哒', 'AEA417E0', 1, '2025-11-30 00:35:11', '2025-11-30 00:35:11', NULL, NULL);

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
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_dish_id`(`dish_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 63 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_items
-- ----------------------------
INSERT INTO `order_items` VALUES (39, 15, 26, '西湖牛肉羹', 'https://dummyimage.com/600x400/ffd43b/ffffff&text=西湖牛肉羹', 32.00, 1, 32.00, '2025-11-29 01:15:50', 1);
INSERT INTO `order_items` VALUES (40, 15, 27, '酸辣汤', 'https://dummyimage.com/600x400/fab005/ffffff&text=酸辣汤', 22.00, 1, 22.00, '2025-11-29 01:15:50', 1);
INSERT INTO `order_items` VALUES (41, 15, 28, '番茄蛋花汤', 'https://dummyimage.com/600x400/ffe066/ffffff&text=番茄蛋花汤', 18.00, 1, 18.00, '2025-11-29 01:15:50', 1);
INSERT INTO `order_items` VALUES (42, 15, 29, '紫菜蛋花汤', 'https://dummyimage.com/600x400/ffd43b/ffffff&text=紫菜蛋花汤', 16.00, 1, 16.00, '2025-11-29 01:15:50', 1);
INSERT INTO `order_items` VALUES (43, 16, 9, '宫保鸡丁', 'https://dummyimage.com/600x400/ff6b6b/ffffff&text=宫保鸡丁', 38.00, 3, 114.00, '2025-11-29 02:02:17', 1);
INSERT INTO `order_items` VALUES (44, 16, 11, '红烧肉', 'https://dummyimage.com/600x400/c92a2a/ffffff&text=红烧肉', 48.00, 1, 48.00, '2025-11-29 02:02:17', 1);
INSERT INTO `order_items` VALUES (55, 19, 9, '宫保鸡丁', 'https://dummyimage.com/600x400/ff6b6b/ffffff&text=宫保鸡丁', 38.00, 1, 38.00, '2025-11-29 15:43:50', 1);
INSERT INTO `order_items` VALUES (56, 19, 19, '夫妻肺片', 'https://dummyimage.com/600x400/51cf66/ffffff&text=夫妻肺片', 36.00, 1, 36.00, '2025-11-29 15:43:50', 1);
INSERT INTO `order_items` VALUES (57, 19, 26, '西湖牛肉羹', 'https://dummyimage.com/600x400/ffd43b/ffffff&text=西湖牛肉羹', 32.00, 1, 32.00, '2025-11-29 15:43:50', 1);
INSERT INTO `order_items` VALUES (58, 19, 32, '扬州炒饭', 'https://dummyimage.com/600x400/74c0fc/ffffff&text=扬州炒饭', 28.00, 1, 28.00, '2025-11-29 15:43:50', 1);
INSERT INTO `order_items` VALUES (59, 19, 55, '烤韭菜', 'https://dummyimage.com/600x400/f59f00/ffffff&text=烤韭菜', 15.00, 1, 15.00, '2025-11-29 15:43:50', 1);
INSERT INTO `order_items` VALUES (60, 19, 57, '红豆双皮奶', 'https://dummyimage.com/600x400/e599f7/ffffff&text=红豆双皮奶', 22.00, 1, 22.00, '2025-11-29 15:43:50', 1);
INSERT INTO `order_items` VALUES (61, 19, 58, '芒果班戟', 'https://dummyimage.com/600x400/da77f2/ffffff&text=芒果班戟', 28.00, 1, 28.00, '2025-11-29 15:43:50', 1);
INSERT INTO `order_items` VALUES (62, 20, 9, '宫保鸡丁', 'https://dummyimage.com/600x400/ff6b6b/ffffff&text=宫保鸡丁', 38.00, 2, 76.00, '2025-11-30 00:39:45', 1);

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
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_number`(`order_number` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (15, 'FM202511290115500001', 3, 88.00, 3, '', '2025-11-29 01:15:50', '2025-11-29 22:27:15', 1);
INSERT INTO `orders` VALUES (16, 'FM202511290202160001', 3, 162.00, 3, '', '2025-11-29 02:02:17', '2025-11-29 22:27:15', 1);
INSERT INTO `orders` VALUES (19, 'FM202511291543490001', 3, 199.00, 0, '', '2025-11-29 15:43:50', '2025-11-29 22:27:15', 1);
INSERT INTO `orders` VALUES (20, 'FM202511300039440001', 3, 76.00, 0, '', '2025-11-30 00:39:45', '2025-11-30 00:39:45', 1);

-- ----------------------------
-- Table structure for system_config
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置键',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置值',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置说明',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_config
-- ----------------------------
INSERT INTO `system_config` VALUES (1, 'dish_image_limit', '5', '菜品图片数量限制', '2025-11-29 03:17:29', '2025-11-29 03:17:29');

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
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  `role` tinyint NULL DEFAULT 0 COMMENT '角色: 0-普通管理员, 1-家庭管理员, 2-超级管理员',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐢ㄦ埛琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13800138000', '管理员', NULL, 1, '2025-11-21 14:48:37', '2025-11-21 14:48:37', 1, 2);
INSERT INTO `user` VALUES (2, 'test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13800138001', '测试用户', NULL, 1, '2025-11-21 14:48:37', '2025-11-21 14:48:37', 1, 0);
INSERT INTO `user` VALUES (3, 'long', '$2a$10$ej.O.Bd2Wcz4J4TCTTxX5..TqFze.IJV2nDiX4arM/kVAI57KKeqC', '18320636653', 'long', '', 1, '2025-11-23 17:44:20', '2025-11-24 23:33:29', 1, 0);

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
  `local_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '本地头像路径',
  `gender` int NULL DEFAULT 0 COMMENT '性别 0:未知 1:男 2:女',
  `role` tinyint NOT NULL DEFAULT 0 COMMENT '角色 0:普通用户 1:管理员',
  `status` int NULL DEFAULT 1 COMMENT '状态 0:禁用 1:正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除',
  `avatar_update_count` int NULL DEFAULT 0 COMMENT '头像更新次数(每日限制)',
  `avatar_last_update_date` date NULL DEFAULT NULL COMMENT '头像最后更新日期',
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_openid`(`openid` ASC) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '微信小程序用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wx_user
-- ----------------------------
INSERT INTO `wx_user` VALUES (1, NULL, NULL, 'testuser', '$2a$10$hVxizz1IIYgR6kfd93omPuFjWpkEMcf5skkXEupGG5zcUSPoF0Xh.', '13900139000', '哈哈哈', 'avatars/2025-11-28/3ef3527e-f3ff-4313-b859-53ab3c9967e0-okAZhN0b9lDNHCAeCsnaAG8QAxv7gAfIsvAAQA_tplv-dy-awe.png', 'avatars/2025-11-28/3ef3527e-f3ff-4313-b859-53ab3c9967e0-okAZhN0b9lDNHCAeCsnaAG8QAxv7gAfIsvAAQA_tplv-dy-awe.png', 0, 1, 1, '2025-11-22 16:13:38', '2025-11-29 22:27:15', 0, 0, NULL, 1);
INSERT INTO `wx_user` VALUES (2, NULL, NULL, '谢谢', '$2a$10$A0L9e3OMOKtTWflA8AfjzeowzbyOpb3mWT.eT3vTWDYv9/uAY7Tpi', '', '大灰狼', 'avatars/2025-11-22/81207505-f172-4b04-9d05-7b474914cd3a-ahC04MiN6Ddpe86ac75c94006bd7a1cd2f069633a210.jpg', NULL, 0, 0, 1, '2025-11-22 17:51:41', '2025-11-29 22:27:15', 0, 0, NULL, 1);
INSERT INTO `wx_user` VALUES (3, NULL, NULL, 'long', '$2a$10$epnCwD.0n2xXz17Q3iv.6OYPrXnpoqpt00H/bKOyqBZ9odtn3lzui', NULL, 'long', 'avatars/2025-11-28/0961a72c-8ee3-47d2-82d1-46d0a4aa62d1-okAZhN0b9lDNHCAeCsnaAG8QAxv7gAfIsvAAQA_tplv-dy-awe.png', 'avatars/2025-11-28/0961a72c-8ee3-47d2-82d1-46d0a4aa62d1-okAZhN0b9lDNHCAeCsnaAG8QAxv7gAfIsvAAQA_tplv-dy-awe.png', 0, 1, 1, '2025-11-23 17:30:49', '2025-11-29 22:27:15', 0, 3, '2025-11-28', 1);

SET FOREIGN_KEY_CHECKS = 1;
