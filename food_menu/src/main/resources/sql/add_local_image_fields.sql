-- 添加本地图片字段到dish表和wx_user表
-- 用于支持OSS和本地存储双URL共存

-- 1. 为dish表添加local_image字段
ALTER TABLE dish ADD COLUMN local_image VARCHAR(255) COMMENT '本地图片路径' AFTER image;

-- 2. 为wx_user表添加local_avatar字段
ALTER TABLE wx_user ADD COLUMN local_avatar VARCHAR(255) COMMENT '本地头像路径' AFTER avatar;

-- 3. 为banner表添加local_image字段
ALTER TABLE banner ADD COLUMN local_image VARCHAR(255) COMMENT '本地图片路径' AFTER image;

-- 查看修改结果
SELECT 'dish表结构修改完成' AS status;
DESCRIBE dish;

SELECT 'wx_user表结构修改完成' AS status;
DESCRIBE wx_user;

SELECT 'banner表结构修改完成' AS status;
DESCRIBE banner;
