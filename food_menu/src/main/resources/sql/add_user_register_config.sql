-- 添加用户注册开关配置
-- 默认值为 'true'，保持现有功能不受影响
INSERT INTO system_config (config_key, config_value, description, create_time, update_time, deleted, family_id)
VALUES ('user_register_enabled', 'true', '微信端用户自主注册开关', NOW(), NOW(), 0, NULL)
ON DUPLICATE KEY UPDATE 
    config_value = VALUES(config_value),
    description = VALUES(description),
    update_time = NOW();
