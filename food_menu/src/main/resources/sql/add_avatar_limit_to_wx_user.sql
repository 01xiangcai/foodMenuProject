ALTER TABLE `wx_user`
ADD COLUMN `avatar_update_count` INT DEFAULT 0 COMMENT '头像更新次数(每日限制)',
ADD COLUMN `avatar_last_update_date` DATE DEFAULT NULL COMMENT '头像最后更新日期';
