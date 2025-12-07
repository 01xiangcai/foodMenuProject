-- 添加支付密码错误次数限制字段
ALTER TABLE `wx_user_wallet` 
ADD COLUMN `pay_password_error_count` INT DEFAULT 0 COMMENT '支付密码当日错误次数',
ADD COLUMN `pay_password_error_date` DATE DEFAULT NULL COMMENT '支付密码错误日期(用于每日重置)';
