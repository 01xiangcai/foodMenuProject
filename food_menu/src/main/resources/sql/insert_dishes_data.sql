-- ===================================================
-- 美食菜单系统 - 菜品分类和菜品数据
-- 生成日期: 2025-11-28
-- 说明: 包含8个分类和50+道菜品
-- ===================================================

SET NAMES utf8mb4;

-- ----------------------------
-- 插入菜品分类数据
-- ----------------------------
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES
(1, 1, '热菜', 1, NOW(), NOW(), 1, 1),
(2, 1, '凉菜', 2, NOW(), NOW(), 1, 1),
(3, 1, '汤羹', 3, NOW(), NOW(), 1, 1),
(4, 1, '主食', 4, NOW(), NOW(), 1, 1),
(5, 1, '素菜', 5, NOW(), NOW(), 1, 1),
(6, 1, '海鲜', 6, NOW(), NOW(), 1, 1),
(7, 1, '烧烤', 7, NOW(), NOW(), 1, 1),
(8, 1, '甜品', 8, NOW(), NOW(), 1, 1);

-- ----------------------------
-- 插入菜品数据 - 热菜类
-- ----------------------------
INSERT INTO `dish` (`name`, `category_id`, `price`, `code`, `image`, `description`, `status`, `sort`, `calories`, `tags`, `create_time`, `update_time`, `create_user`, `update_user`, `is_deleted`) VALUES
('宫保鸡丁', 1, 38.00, 'RC001', 'https://dummyimage.com/600x400/ff6b6b/ffffff&text=宫保鸡丁', '经典川菜，鸡肉鲜嫩，花生香脆，酸甜微辣', 1, 1, '320千卡', '川菜,辣,下饭', NOW(), NOW(), 1, 1, 0),
('鱼香肉丝', 1, 32.00, 'RC002', 'https://dummyimage.com/600x400/ff8787/ffffff&text=鱼香肉丝', '肉丝滑嫩，酱汁浓郁，鱼香味十足', 1, 2, '280千卡', '川菜,酸甜,家常', NOW(), NOW(), 1, 1, 0),
('红烧肉', 1, 48.00, 'RC003', 'https://dummyimage.com/600x400/c92a2a/ffffff&text=红烧肉', '肥而不腻，入口即化，色泽红亮', 1, 3, '450千卡', '本帮菜,甜,经典', NOW(), NOW(), 1, 1, 0),
('麻婆豆腐', 1, 28.00, 'RC004', 'https://dummyimage.com/600x400/fa5252/ffffff&text=麻婆豆腐', '麻辣鲜香，豆腐嫩滑，下饭神器', 1, 4, '220千卡', '川菜,麻辣,素食', NOW(), NOW(), 1, 1, 0),
('糖醋里脊', 1, 42.00, 'RC005', 'https://dummyimage.com/600x400/ff6b6b/ffffff&text=糖醋里脊', '外酥里嫩，酸甜可口，色泽金黄', 1, 5, '380千卡', '鲁菜,酸甜,儿童最爱', NOW(), NOW(), 1, 1, 0),
('水煮鱼', 1, 68.00, 'RC006', 'https://dummyimage.com/600x400/e03131/ffffff&text=水煮鱼', '鱼肉鲜嫩，麻辣鲜香，汤汁浓郁', 1, 6, '420千卡', '川菜,麻辣,招牌', NOW(), NOW(), 1, 1, 0),
('回锅肉', 1, 45.00, 'RC007', 'https://dummyimage.com/600x400/f03e3e/ffffff&text=回锅肉', '肥瘦相间，香辣下饭，川菜经典', 1, 7, '480千卡', '川菜,辣,经典', NOW(), NOW(), 1, 1, 0),
('东坡肉', 1, 58.00, 'RC008', 'https://dummyimage.com/600x400/c92a2a/ffffff&text=东坡肉', '酥烂而形不碎，香糯而不腻口', 1, 8, '520千卡', '浙菜,甜,特色', NOW(), NOW(), 1, 1, 0),
('小炒肉', 1, 35.00, 'RC009', 'https://dummyimage.com/600x400/ff6b6b/ffffff&text=小炒肉', '湘菜经典，肉片嫩滑，辣椒香脆', 1, 9, '340千卡', '湘菜,辣,家常', NOW(), NOW(), 1, 1, 0),
('青椒肉丝', 1, 30.00, 'RC010', 'https://dummyimage.com/600x400/ff8787/ffffff&text=青椒肉丝', '家常小炒，简单美味，营养均衡', 1, 10, '260千卡', '家常菜,微辣,健康', NOW(), NOW(), 1, 1, 0);

-- ----------------------------
-- 插入菜品数据 - 凉菜类
-- ----------------------------
INSERT INTO `dish` (`name`, `category_id`, `price`, `code`, `image`, `description`, `status`, `sort`, `calories`, `tags`, `create_time`, `update_time`, `create_user`, `update_user`, `is_deleted`) VALUES
('夫妻肺片', 2, 36.00, 'LC001', 'https://dummyimage.com/600x400/51cf66/ffffff&text=夫妻肺片', '麻辣鲜香，色泽红亮，质地软嫩', 1, 1, '280千卡', '川菜,麻辣,凉菜', NOW(), NOW(), 1, 1, 0),
('口水鸡', 2, 38.00, 'LC002', 'https://dummyimage.com/600x400/69db7c/ffffff&text=口水鸡', '鸡肉鲜嫩，麻辣鲜香，让人垂涎', 1, 2, '240千卡', '川菜,麻辣,开胃', NOW(), NOW(), 1, 1, 0),
('凉拌黄瓜', 2, 18.00, 'LC003', 'https://dummyimage.com/600x400/8ce99a/ffffff&text=凉拌黄瓜', '清爽脆嫩，酸甜可口，解腻开胃', 1, 3, '45千卡', '素食,清淡,健康', NOW(), NOW(), 1, 1, 0),
('皮蛋豆腐', 2, 22.00, 'LC004', 'https://dummyimage.com/600x400/b2f2bb/ffffff&text=皮蛋豆腐', '豆腐嫩滑，皮蛋香醇，清爽可口', 1, 4, '180千卡', '家常菜,清淡,营养', NOW(), NOW(), 1, 1, 0),
('拍黄瓜', 2, 16.00, 'LC005', 'https://dummyimage.com/600x400/8ce99a/ffffff&text=拍黄瓜', '简单美味，蒜香浓郁，清脆爽口', 1, 5, '40千卡', '素食,清淡,开胃', NOW(), NOW(), 1, 1, 0),
('凉拌木耳', 2, 20.00, 'LC006', 'https://dummyimage.com/600x400/69db7c/ffffff&text=凉拌木耳', '口感脆爽，营养丰富，清淡健康', 1, 6, '55千卡', '素食,健康,清淡', NOW(), NOW(), 1, 1, 0),
('蒜泥白肉', 2, 42.00, 'LC007', 'https://dummyimage.com/600x400/51cf66/ffffff&text=蒜泥白肉', '肉片薄而不散，蒜香浓郁，肥而不腻', 1, 7, '320千卡', '川菜,蒜香,经典', NOW(), NOW(), 1, 1, 0);

-- ----------------------------
-- 插入菜品数据 - 汤羹类
-- ----------------------------
INSERT INTO `dish` (`name`, `category_id`, `price`, `code`, `image`, `description`, `status`, `sort`, `calories`, `tags`, `create_time`, `update_time`, `create_user`, `update_user`, `is_deleted`) VALUES
('西湖牛肉羹', 3, 32.00, 'TG001', 'https://dummyimage.com/600x400/ffd43b/ffffff&text=西湖牛肉羹', '汤汁浓稠，牛肉鲜嫩，营养丰富', 1, 1, '180千卡', '浙菜,营养,暖胃', NOW(), NOW(), 1, 1, 0),
('酸辣汤', 3, 22.00, 'TG002', 'https://dummyimage.com/600x400/fab005/ffffff&text=酸辣汤', '酸辣开胃，汤汁浓郁，暖胃驱寒', 1, 2, '120千卡', '川菜,酸辣,开胃', NOW(), NOW(), 1, 1, 0),
('番茄蛋花汤', 3, 18.00, 'TG003', 'https://dummyimage.com/600x400/ffe066/ffffff&text=番茄蛋花汤', '酸甜可口，营养丰富，老少皆宜', 1, 3, '90千卡', '家常菜,营养,清淡', NOW(), NOW(), 1, 1, 0),
('紫菜蛋花汤', 3, 16.00, 'TG004', 'https://dummyimage.com/600x400/ffd43b/ffffff&text=紫菜蛋花汤', '清淡鲜美，营养丰富，简单美味', 1, 4, '75千卡', '家常菜,清淡,营养', NOW(), NOW(), 1, 1, 0),
('玉米排骨汤', 3, 38.00, 'TG005', 'https://dummyimage.com/600x400/fab005/ffffff&text=玉米排骨汤', '排骨酥烂，玉米香甜，汤汁鲜美', 1, 5, '220千卡', '粤菜,营养,滋补', NOW(), NOW(), 1, 1, 0),
('冬瓜排骨汤', 3, 35.00, 'TG006', 'https://dummyimage.com/600x400/ffe066/ffffff&text=冬瓜排骨汤', '清热解暑，营养滋补，汤汁清甜', 1, 6, '180千卡', '粤菜,清淡,养生', NOW(), NOW(), 1, 1, 0);

-- ----------------------------
-- 插入菜品数据 - 主食类
-- ----------------------------
INSERT INTO `dish` (`name`, `category_id`, `price`, `code`, `image`, `description`, `status`, `sort`, `calories`, `tags`, `create_time`, `update_time`, `create_user`, `update_user`, `is_deleted`) VALUES
('扬州炒饭', 4, 28.00, 'ZS001', 'https://dummyimage.com/600x400/74c0fc/ffffff&text=扬州炒饭', '粒粒分明，配料丰富，色香味俱全', 1, 1, '480千卡', '主食,炒饭,经典', NOW(), NOW(), 1, 1, 0),
('蛋炒饭', 4, 18.00, 'ZS002', 'https://dummyimage.com/600x400/4dabf7/ffffff&text=蛋炒饭', '简单美味，蛋香浓郁，家常必备', 1, 2, '420千卡', '主食,炒饭,家常', NOW(), NOW(), 1, 1, 0),
('牛肉面', 4, 32.00, 'ZS003', 'https://dummyimage.com/600x400/339af0/ffffff&text=牛肉面', '牛肉酥烂，汤汁浓郁，面条劲道', 1, 3, '520千卡', '主食,面食,营养', NOW(), NOW(), 1, 1, 0),
('小笼包', 4, 25.00, 'ZS004', 'https://dummyimage.com/600x400/74c0fc/ffffff&text=小笼包', '皮薄馅大，汤汁鲜美，江南名点', 1, 4, '280千卡', '主食,点心,特色', NOW(), NOW(), 1, 1, 0),
('煎饺', 4, 22.00, 'ZS005', 'https://dummyimage.com/600x400/4dabf7/ffffff&text=煎饺', '底部金黄酥脆，馅料鲜美多汁', 1, 5, '320千卡', '主食,点心,香脆', NOW(), NOW(), 1, 1, 0),
('葱油拌面', 4, 20.00, 'ZS006', 'https://dummyimage.com/600x400/339af0/ffffff&text=葱油拌面', '葱香浓郁，面条爽滑，简单美味', 1, 6, '380千卡', '主食,面食,清淡', NOW(), NOW(), 1, 1, 0),
('馄饨', 4, 24.00, 'ZS007', 'https://dummyimage.com/600x400/74c0fc/ffffff&text=馄饨', '皮薄馅嫩，汤汁鲜美，温暖贴心', 1, 7, '260千卡', '主食,点心,营养', NOW(), NOW(), 1, 1, 0);

-- ----------------------------
-- 插入菜品数据 - 素菜类
-- ----------------------------
INSERT INTO `dish` (`name`, `category_id`, `price`, `code`, `image`, `description`, `status`, `sort`, `calories`, `tags`, `create_time`, `update_time`, `create_user`, `update_user`, `is_deleted`) VALUES
('地三鲜', 5, 28.00, 'SC001', 'https://dummyimage.com/600x400/94d82d/ffffff&text=地三鲜', '茄子、土豆、青椒，东北名菜', 1, 1, '320千卡', '东北菜,素食,下饭', NOW(), NOW(), 1, 1, 0),
('干煸豆角', 5, 26.00, 'SC002', 'https://dummyimage.com/600x400/a9e34b/ffffff&text=干煸豆角', '豆角干香，口感脆嫩，川菜经典', 1, 2, '180千卡', '川菜,素食,香脆', NOW(), NOW(), 1, 1, 0),
('清炒时蔬', 5, 22.00, 'SC003', 'https://dummyimage.com/600x400/c0eb75/ffffff&text=清炒时蔬', '新鲜时令蔬菜，清淡健康，营养丰富', 1, 3, '85千卡', '素食,清淡,健康', NOW(), NOW(), 1, 1, 0),
('蒜蓉西兰花', 5, 24.00, 'SC004', 'https://dummyimage.com/600x400/d8f5a2/ffffff&text=蒜蓉西兰花', '西兰花翠绿，蒜香浓郁，营养丰富', 1, 4, '95千卡', '素食,健康,蒜香', NOW(), NOW(), 1, 1, 0),
('手撕包菜', 5, 20.00, 'SC005', 'https://dummyimage.com/600x400/94d82d/ffffff&text=手撕包菜', '包菜脆嫩，酸辣开胃，简单美味', 1, 5, '75千卡', '素食,酸辣,开胃', NOW(), NOW(), 1, 1, 0),
('炒土豆丝', 5, 18.00, 'SC006', 'https://dummyimage.com/600x400/a9e34b/ffffff&text=炒土豆丝', '土豆丝脆爽，酸辣可口，家常必备', 1, 6, '120千卡', '素食,家常,清淡', NOW(), NOW(), 1, 1, 0),
('蚝油生菜', 5, 22.00, 'SC007', 'https://dummyimage.com/600x400/c0eb75/ffffff&text=蚝油生菜', '生菜翠绿，蚝油鲜香，清淡爽口', 1, 7, '65千卡', '素食,清淡,鲜香', NOW(), NOW(), 1, 1, 0);

-- ----------------------------
-- 插入菜品数据 - 海鲜类
-- ----------------------------
INSERT INTO `dish` (`name`, `category_id`, `price`, `code`, `image`, `description`, `status`, `sort`, `calories`, `tags`, `create_time`, `update_time`, `create_user`, `update_user`, `is_deleted`) VALUES
('清蒸鲈鱼', 6, 88.00, 'HX001', 'https://dummyimage.com/600x400/3bc9db/ffffff&text=清蒸鲈鱼', '鱼肉鲜嫩，原汁原味，营养丰富', 1, 1, '180千卡', '粤菜,海鲜,清淡', NOW(), NOW(), 1, 1, 0),
('红烧大虾', 6, 98.00, 'HX002', 'https://dummyimage.com/600x400/22b8cf/ffffff&text=红烧大虾', '虾肉Q弹，酱汁浓郁，色泽红亮', 1, 2, '220千卡', '鲁菜,海鲜,鲜美', NOW(), NOW(), 1, 1, 0),
('蒜蓉粉丝蒸扇贝', 6, 78.00, 'HX003', 'https://dummyimage.com/600x400/15aabf/ffffff&text=蒜蓉粉丝蒸扇贝', '扇贝鲜美，蒜香浓郁，粉丝爽滑', 1, 3, '160千卡', '粤菜,海鲜,蒜香', NOW(), NOW(), 1, 1, 0),
('椒盐皮皮虾', 6, 68.00, 'HX004', 'https://dummyimage.com/600x400/3bc9db/ffffff&text=椒盐皮皮虾', '外酥里嫩，椒盐香脆，鲜香可口', 1, 4, '240千卡', '粤菜,海鲜,香脆', NOW(), NOW(), 1, 1, 0),
('爆炒鱿鱼', 6, 58.00, 'HX005', 'https://dummyimage.com/600x400/22b8cf/ffffff&text=爆炒鱿鱼', '鱿鱼爽脆，酱汁浓郁，鲜香美味', 1, 5, '200千卡', '川菜,海鲜,鲜辣', NOW(), NOW(), 1, 1, 0),
('蒜蓉开背虾', 6, 108.00, 'HX006', 'https://dummyimage.com/600x400/15aabf/ffffff&text=蒜蓉开背虾', '虾肉饱满，蒜香浓郁，鲜美诱人', 1, 6, '260千卡', '粤菜,海鲜,蒜香', NOW(), NOW(), 1, 1, 0);

-- ----------------------------
-- 插入菜品数据 - 烧烤类
-- ----------------------------
INSERT INTO `dish` (`name`, `category_id`, `price`, `code`, `image`, `description`, `status`, `sort`, `calories`, `tags`, `create_time`, `update_time`, `create_user`, `update_user`, `is_deleted`) VALUES
('烤羊肉串', 7, 38.00, 'SK001', 'https://dummyimage.com/600x400/f59f00/ffffff&text=烤羊肉串', '肉质鲜嫩，孜然香浓，新疆风味', 1, 1, '280千卡', '烧烤,羊肉,香辣', NOW(), NOW(), 1, 1, 0),
('烤鸡翅', 7, 32.00, 'SK002', 'https://dummyimage.com/600x400/f76707/ffffff&text=烤鸡翅', '外焦里嫩，香气扑鼻，人气单品', 1, 2, '240千卡', '烧烤,鸡肉,香脆', NOW(), NOW(), 1, 1, 0),
('烤茄子', 7, 18.00, 'SK003', 'https://dummyimage.com/600x400/fd7e14/ffffff&text=烤茄子', '茄子软烂，蒜香浓郁，素食之选', 1, 3, '120千卡', '烧烤,素食,蒜香', NOW(), NOW(), 1, 1, 0),
('烤韭菜', 7, 15.00, 'SK004', 'https://dummyimage.com/600x400/f59f00/ffffff&text=烤韭菜', '韭菜鲜嫩，香气四溢，开胃下酒', 1, 4, '65千卡', '烧烤,素食,清香', NOW(), NOW(), 1, 1, 0),
('烤五花肉', 7, 42.00, 'SK005', 'https://dummyimage.com/600x400/f76707/ffffff&text=烤五花肉', '肥瘦相间，香脆可口，肉香浓郁', 1, 5, '420千卡', '烧烤,猪肉,香脆', NOW(), NOW(), 1, 1, 0);

-- ----------------------------
-- 插入菜品数据 - 甜品类
-- ----------------------------
INSERT INTO `dish` (`name`, `category_id`, `price`, `code`, `image`, `description`, `status`, `sort`, `calories`, `tags`, `create_time`, `update_time`, `create_user`, `update_user`, `is_deleted`) VALUES
('红豆双皮奶', 8, 22.00, 'TP001', 'https://dummyimage.com/600x400/e599f7/ffffff&text=红豆双皮奶', '奶香浓郁，口感细腻，甜而不腻', 1, 1, '280千卡', '甜品,奶制品,经典', NOW(), NOW(), 1, 1, 0),
('芒果班戟', 8, 28.00, 'TP002', 'https://dummyimage.com/600x400/da77f2/ffffff&text=芒果班戟', '芒果香甜，奶油细腻，清新可口', 1, 2, '320千卡', '甜品,水果,清新', NOW(), NOW(), 1, 1, 0),
('杨枝甘露', 8, 26.00, 'TP003', 'https://dummyimage.com/600x400/cc5de8/ffffff&text=杨枝甘露', '芒果西柚，清爽甜蜜，港式经典', 1, 3, '240千卡', '甜品,水果,清爽', NOW(), NOW(), 1, 1, 0),
('提拉米苏', 8, 32.00, 'TP004', 'https://dummyimage.com/600x400/e599f7/ffffff&text=提拉米苏', '咖啡香浓，芝士细腻，意式经典', 1, 4, '380千卡', '甜品,咖啡,浓郁', NOW(), NOW(), 1, 1, 0),
('冰糖雪梨', 8, 18.00, 'TP005', 'https://dummyimage.com/600x400/da77f2/ffffff&text=冰糖雪梨', '清甜润肺，养生佳品，清热解暑', 1, 5, '120千卡', '甜品,养生,清润', NOW(), NOW(), 1, 1, 0),
('龟苓膏', 8, 16.00, 'TP006', 'https://dummyimage.com/600x400/cc5de8/ffffff&text=龟苓膏', '清热解毒，口感Q弹，养生甜品', 1, 6, '95千卡', '甜品,养生,清热', NOW(), NOW(), 1, 1, 0);

-- ----------------------------
-- 说明
-- ----------------------------
-- 1. 以上SQL已插入8个分类，共57道菜品
-- 2. 每道菜品都包含：名称、分类、价格、编码、图片、描述、卡路里、标签等信息
-- 3. 图片使用占位图，实际使用时请替换为真实图片URL
-- 4. 所有菜品状态默认为启用(status=1)，未删除(is_deleted=0)
-- 5. 可根据实际需求调整价格、描述等信息

