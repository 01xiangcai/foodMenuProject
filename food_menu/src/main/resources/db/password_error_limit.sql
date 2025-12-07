-- 添加登录密码错误次数限制字段
ALTER TABLE `wx_user` 
ADD COLUMN `password_error_count` INT DEFAULT 0 COMMENT '登录密码当日错误次数',
ADD COLUMN `password_error_date` DATE DEFAULT NULL COMMENT '登录密码错误日期(用于每日重置)';
