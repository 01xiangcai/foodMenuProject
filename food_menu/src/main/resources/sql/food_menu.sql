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

 Date: 16/12/2025 01:35:08
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity_participate_record
-- ----------------------------
DROP TABLE IF EXISTS `activity_participate_record`;
CREATE TABLE `activity_participate_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `wx_user_id` bigint NOT NULL COMMENT '参与用户ID',
  `participate_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '参与时间',
  `prize_id` bigint NULL DEFAULT NULL COMMENT '中奖奖品ID(NULL表示未中奖)',
  `is_win` tinyint NOT NULL DEFAULT 0 COMMENT '是否中奖: 0-未中奖, 1-已中奖',
  `prize_status` tinyint NOT NULL DEFAULT 0 COMMENT '奖品状态: 0-未领取, 1-已领取, 2-已使用, 3-已过期',
  `claim_time` datetime NULL DEFAULT NULL COMMENT '领取时间',
  `use_time` datetime NULL DEFAULT NULL COMMENT '使用时间',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间',
  `extra_data` json NULL COMMENT '额外数据(如抽奖结果详情)',
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_activity_user`(`activity_id` ASC, `wx_user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`wx_user_id` ASC) USING BTREE,
  INDEX `idx_participate_time`(`participate_time` ASC) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE,
  INDEX `fk_record_prize`(`prize_id` ASC) USING BTREE,
  CONSTRAINT `fk_record_activity` FOREIGN KEY (`activity_id`) REFERENCES `marketing_activity` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_record_prize` FOREIGN KEY (`prize_id`) REFERENCES `activity_prize` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动参与记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for activity_prize
-- ----------------------------
DROP TABLE IF EXISTS `activity_prize`;
CREATE TABLE `activity_prize`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '奖品ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `prize_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '奖品名称',
  `prize_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '奖品类型: COUPON(优惠券), POINTS(积分), DISH(菜品), PHYSICAL(实物), THANK_YOU(谢谢参与)',
  `prize_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '奖品图片',
  `local_prize_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '本地奖品图片路径',
  `prize_value` decimal(10, 2) NULL DEFAULT NULL COMMENT '奖品价值',
  `prize_config` json NULL COMMENT '奖品配置(如优惠券ID、菜品ID等)',
  `total_quantity` int NOT NULL DEFAULT -1 COMMENT '总数量(-1表示无限)',
  `remain_quantity` int NOT NULL DEFAULT -1 COMMENT '剩余数量',
  `win_probability` decimal(5, 4) NULL DEFAULT NULL COMMENT '中奖概率(0.0001-1.0000)',
  `prize_level` int NOT NULL DEFAULT 0 COMMENT '奖品等级(用于排序展示)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_activity_id`(`activity_id` ASC) USING BTREE,
  CONSTRAINT `fk_prize_activity` FOREIGN KEY (`activity_id`) REFERENCES `marketing_activity` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动奖品表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for activity_statistics
-- ----------------------------
DROP TABLE IF EXISTS `activity_statistics`;
CREATE TABLE `activity_statistics`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `total_participants` int NOT NULL DEFAULT 0 COMMENT '总参与人数',
  `total_participations` int NOT NULL DEFAULT 0 COMMENT '总参与次数',
  `total_winners` int NOT NULL DEFAULT 0 COMMENT '总中奖人数',
  `total_wins` int NOT NULL DEFAULT 0 COMMENT '总中奖次数',
  `total_prize_value` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '总奖品价值',
  `conversion_rate` decimal(5, 4) NOT NULL DEFAULT 0.0000 COMMENT '转化率',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_activity_date`(`activity_id` ASC, `stat_date` ASC) USING BTREE,
  CONSTRAINT `fk_statistics_activity` FOREIGN KEY (`activity_id`) REFERENCES `marketing_activity` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动统计表' ROW_FORMAT = DYNAMIC;

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
  `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '轮播图表' ROW_FORMAT = Dynamic;

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
  `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_category_name`(`name` ASC) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜品分类' ROW_FORMAT = Dynamic;

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
  `image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片',
  `local_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '本地图片路径',
  `local_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '所有图片路径JSON数组',
  `description` varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鎻忚堪淇℃伅',
  `status` int NOT NULL DEFAULT 1 COMMENT '0:鍋滃敭 1:璧峰敭',
  `sort` int NOT NULL DEFAULT 0 COMMENT '椤哄簭',
  `create_time` datetime NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `create_user` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜',
  `update_user` bigint NULL DEFAULT NULL COMMENT '淇?敼浜',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
  `calories` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '卡路里',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签',
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 71 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜品' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for dish_category
-- ----------------------------
DROP TABLE IF EXISTS `dish_category`;
CREATE TABLE `dish_category`  (
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
  UNIQUE INDEX `uk_dish_category`(`dish_id` ASC, `category_id` ASC, `deleted` ASC) USING BTREE COMMENT '菜品-分类唯一索引(包含删除标记)',
  INDEX `idx_dish_id`(`dish_id` ASC) USING BTREE COMMENT '菜品ID索引',
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE COMMENT '分类ID索引',
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE COMMENT '家庭ID索引'
) ENGINE = InnoDB AUTO_INCREMENT = 66 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜品分类关联表' ROW_FORMAT = DYNAMIC;

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
  `reply_to_user_id` bigint NULL DEFAULT NULL COMMENT '被回复人用户ID',
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE,
  INDEX `idx_reply_to_user_id`(`reply_to_user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜品评论表' ROW_FORMAT = Dynamic;

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
  `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_dish`(`user_id` ASC, `dish_id` ASC) USING BTREE COMMENT '用户-菜品唯一索引',
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE COMMENT '用户ID索引',
  INDEX `idx_dish_id`(`dish_id` ASC) USING BTREE COMMENT '菜品ID索引',
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 53 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜品收藏表' ROW_FORMAT = Dynamic;

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
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 60 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鑿滃搧鍙ｅ懗琛' ROW_FORMAT = Dynamic;

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
  `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dish_id`(`dish_id` ASC) USING BTREE COMMENT '菜品ID唯一索引',
  INDEX `idx_total_count`(`total_order_count` DESC) USING BTREE COMMENT '总次数索引',
  INDEX `idx_month_count`(`month_order_count` DESC) USING BTREE COMMENT '月度次数索引',
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 94 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜品统计表' ROW_FORMAT = DYNAMIC;

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
  `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name` ASC) USING BTREE COMMENT '标签名称唯一索引',
  INDEX `idx_type`(`type` ASC) USING BTREE COMMENT '类型索引',
  INDEX `idx_status`(`status` ASC) USING BTREE COMMENT '状态索引',
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜品标签配置表' ROW_FORMAT = DYNAMIC;

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
  `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `invite_code`(`invite_code` ASC) USING BTREE,
  UNIQUE INDEX `uk_invite_code`(`invite_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '家庭表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for marketing_activity
-- ----------------------------
DROP TABLE IF EXISTS `marketing_activity`;
CREATE TABLE `marketing_activity`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `family_id` bigint NOT NULL COMMENT '家庭ID',
  `activity_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动名称',
  `activity_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动类型: LOTTERY(抽奖), WHEEL(大转盘), COUPON(优惠券), POINTS_EXCHANGE(积分兑换), SIGN_IN(签到), GROUP_BUY(拼团)',
  `activity_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '活动描述',
  `banner_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '活动横幅图片',
  `local_banner_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '本地横幅图片路径',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0-未开始, 1-进行中, 2-已结束, 3-已暂停',
  `participate_limit` int NOT NULL DEFAULT 0 COMMENT '参与次数限制(0表示不限制)',
  `limit_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'DAILY' COMMENT '限制类型: DAILY(每日), TOTAL(总计), UNLIMITED(不限)',
  `participate_condition` json NULL COMMENT '参与条件配置(JSON格式)',
  `activity_config` json NOT NULL COMMENT '活动配置(JSON格式,不同活动类型配置不同)',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序权重',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE,
  INDEX `idx_activity_type`(`activity_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_time`(`start_time` ASC, `end_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '营销活动主表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for operation_log
-- ----------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `operation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作类型',
  `operation_module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作模块',
  `operation_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作描述',
  `method_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '方法名',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求方式',
  `request_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求URL',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求参数',
  `response_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '响应结果',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人姓名',
  `operator_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人类型',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户代理',
  `execution_time` bigint NULL DEFAULT NULL COMMENT '执行时长(毫秒)',
  `status` int NULL DEFAULT 1 COMMENT '状态',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误信息',
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_operator_id`(`operator_id` ASC) USING BTREE,
  INDEX `idx_operation_type`(`operation_type` ASC) USING BTREE,
  INDEX `idx_operation_module`(`operation_module` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 97 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
  `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_dish_id`(`dish_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 120 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_number` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单编号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '订单状态 0-待接单 1-准备中 2-配送中 3-已完成 4-已取消 5-待支付',
  `accept_time` datetime NULL DEFAULT NULL COMMENT '接单时间',
  `delivery_time` datetime NULL DEFAULT NULL COMMENT '配送时间',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '订单备注',
  `pay_method` tinyint NULL DEFAULT NULL COMMENT '支付方式: 1=余额支付, 2=模拟支付',
  `pay_status` tinyint NULL DEFAULT 0 COMMENT '支付状态: 0=未支付, 1=已支付, 2=已退款',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_number`(`order_number` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

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
  `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key` ASC) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鐢ㄦ埛鍚',
  `password` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '瀵嗙爜(鍔犲瘑)',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '濮撳悕',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '澶村儚',
  `status` int NULL DEFAULT 1 COMMENT '鐘舵? 0:绂佺敤 1:姝ｅ父',
  `create_time` datetime NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  `role` tinyint NULL DEFAULT 0 COMMENT '角色: 0-普通管理员, 1-家庭管理员, 2-超级管理员',
  `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐢ㄦ埛琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_coupon
-- ----------------------------
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `wx_user_id` bigint NOT NULL COMMENT '用户ID',
  `coupon_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '优惠券名称',
  `coupon_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '优惠券类型: DISCOUNT(折扣券), CASH(代金券), FREE_DISH(免费菜品券)',
  `coupon_value` decimal(10, 2) NOT NULL COMMENT '优惠券面值',
  `min_order_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '最低消费金额',
  `source_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源类型: ACTIVITY(活动), MANUAL(手动发放), SYSTEM(系统赠送)',
  `source_id` bigint NULL DEFAULT NULL COMMENT '来源ID(如活动ID)',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0-未使用, 1-已使用, 2-已过期',
  `receive_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `use_time` datetime NULL DEFAULT NULL COMMENT '使用时间',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `order_id` bigint NULL DEFAULT NULL COMMENT '使用的订单ID',
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`wx_user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_expire_time`(`expire_time` ASC) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户优惠券表' ROW_FORMAT = DYNAMIC;

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
  `password_error_count` int NULL DEFAULT 0 COMMENT '登录密码当日错误次数',
  `password_error_date` date NULL DEFAULT NULL COMMENT '登录密码错误日期(用于每日重置)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_openid`(`openid` ASC) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '微信小程序用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for wx_user_wallet
-- ----------------------------
DROP TABLE IF EXISTS `wx_user_wallet`;
CREATE TABLE `wx_user_wallet`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `wx_user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联微信用户ID',
  `balance` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '可用余额',
  `frozen_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '冻结金额',
  `pay_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付密码(BCrypt加密)',
  `version` int NOT NULL DEFAULT 1 COMMENT '乐观锁版本号',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  `pay_password_error_count` int NULL DEFAULT 0 COMMENT '支付密码当日错误次数',
  `pay_password_error_date` date NULL DEFAULT NULL COMMENT '支付密码错误日期(用于每日重置)',
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_wx_user_id`(`wx_user_id` ASC) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户钱包表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for wx_wallet_transaction
-- ----------------------------
DROP TABLE IF EXISTS `wx_wallet_transaction`;
CREATE TABLE `wx_wallet_transaction`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `wx_user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联微信用户ID',
  `trans_type` tinyint NOT NULL COMMENT '交易类型: 1=后台充值, 2=订单消费, 3=退款冻结，4=取消退款',
  `amount` decimal(12, 2) NOT NULL COMMENT '变动金额',
  `balance_after` decimal(12, 2) NULL DEFAULT NULL COMMENT '交易后余额',
  `related_order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联业务单号',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注说明',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_wx_user_id`(`wx_user_id` ASC) USING BTREE,
  INDEX `idx_trans_type`(`trans_type` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '钱包流水表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
