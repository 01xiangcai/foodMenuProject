1062 - Duplicate entry '89-11' for key 'dish_category.uk_dish_category'-- =============================================
-- 聊天模块数据库表结构
-- 版本: V2.0
-- 创建时间: 2025-12-19
-- 说明: 支持私聊、家庭群聊、消息撤回、已读状态
-- =============================================

-- -------------------------------------------
-- 1. 聊天会话表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `chat_conversation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `type` TINYINT NOT NULL DEFAULT 1 COMMENT '会话类型: 1-私聊, 2-家庭群聊',
    `family_id` BIGINT NULL COMMENT '所属家庭ID（群聊时必填）',
    `name` VARCHAR(100) NULL COMMENT '会话名称（群聊名，私聊时为空）',
    `avatar` VARCHAR(500) NULL COMMENT '会话头像（群聊头像）',
    `last_message_id` BIGINT NULL COMMENT '最后一条消息ID',
    `last_message_time` DATETIME NULL COMMENT '最后消息时间（用于排序）',
    `last_message_content` VARCHAR(200) NULL COMMENT '最后消息内容预览',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_family_id` (`family_id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_last_message_time` (`last_message_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- -------------------------------------------
-- 2. 会话成员表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `chat_conversation_member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
    `wx_user_id` BIGINT NOT NULL COMMENT '用户ID',
    `nickname` VARCHAR(50) NULL COMMENT '用户在会话中的昵称（可选）',
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色: 0-普通成员, 1-群主/管理员',
    `last_read_message_id` BIGINT NOT NULL DEFAULT 0 COMMENT '最后已读消息ID',
    `unread_count` INT NOT NULL DEFAULT 0 COMMENT '未读消息数',
    `muted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否免打扰: 0-否, 1-是',
    `join_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_conversation_user` (`conversation_id`, `wx_user_id`),
    INDEX `idx_wx_user_id` (`wx_user_id`),
    INDEX `idx_conversation_id` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话成员表';

-- -------------------------------------------
-- 3. 聊天消息表（主表，后续按月分表）
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `type` TINYINT NOT NULL DEFAULT 1 COMMENT '消息类型: 1-文本, 2-图片, 3-语音, 4-表情, 5-系统消息',
    `extra` JSON NULL COMMENT '扩展信息（图片尺寸、语音时长等）',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '消息状态: 0-正常, 1-已撤回',
    `reply_to_id` BIGINT NULL COMMENT '回复的消息ID（用于引用回复）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_conversation_id` (`conversation_id`),
    INDEX `idx_sender_id` (`sender_id`),
    INDEX `idx_create_time` (`create_time` DESC),
    INDEX `idx_conversation_time` (`conversation_id`, `create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- -------------------------------------------
-- 4. 新增系统配置项
-- -------------------------------------------
INSERT INTO `system_config` (`config_key`, `config_value`, `description`, `create_time`, `update_time`) 
VALUES 
    ('chat_revoke_time_limit', '2', '消息撤回时限（分钟）', NOW(), NOW()),
    ('chat_message_retention_days', '30', '聊天消息保留天数', NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- -------------------------------------------
-- 5. 消息分表模板（按月创建）
-- 示例：chat_message_202412
-- 生产环境通过定时任务自动创建下月分表
-- -------------------------------------------
-- CREATE TABLE IF NOT EXISTS `chat_message_202412` LIKE `chat_message`;

-- -------------------------------------------
-- 6. 创建消息清理事件（可选，用于自动删除过期消息）
-- -------------------------------------------
-- DELIMITER $$
-- CREATE EVENT IF NOT EXISTS `event_clean_old_chat_messages`
-- ON SCHEDULE EVERY 1 DAY
-- STARTS CURRENT_TIMESTAMP
-- DO
-- BEGIN
--     DECLARE retention_days INT;
--     SELECT CAST(config_value AS UNSIGNED) INTO retention_days 
--     FROM system_config WHERE config_key = 'chat_message_retention_days';
--     
--     IF retention_days > 0 THEN
--         DELETE FROM chat_message 
--         WHERE create_time < DATE_SUB(NOW(), INTERVAL retention_days DAY)
--         AND deleted = 0
--         LIMIT 10000;
--     END IF;
-- END$$
-- DELIMITER ;
