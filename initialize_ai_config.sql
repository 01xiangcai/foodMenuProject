-- 初始化本地 AI 客服配置
-- 1. 插入/更新 AppKey (请替换为您的真实 Key，或者稍后在页面上配置)
INSERT INTO system_config (config_key, config_value, description, family_id)
VALUES ('ai_external_app_key', 'ak_jZfYc2Xbtyx1DdTWtUy80LFF', 'AI客服应用密钥', NULL)
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

-- 2. 插入/更新 BaseUrl (本地环境建议先为空，由前端自动识别，或填 http://localhost:9900)
INSERT INTO system_config (config_key, config_value, description, family_id)
VALUES ('ai_external_base_url', 'http://localhost:9900', 'AI客服服务地址', NULL)
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

-- 3. 验证插入结果
SELECT * FROM system_config WHERE config_key LIKE 'ai_external_%';
