-- 更新 AI 客服的生产环境基准地址
-- 请将 'http://your-server-ip:9900' 替换为您服务器的真实公网 IP 或域名
-- 如果是通过 Nginx 转发的且使用了域名，请确保端口正确

-- 1. 首先检查是否存在该配置项
SELECT * FROM system_config WHERE config_key = 'ai_external_base_url';

-- 2. 如果已存在，更新它
UPDATE system_config 
SET config_value = 'http://localhost:9900' -- 这里虽然写 localhost，但前端现在会自动识别，不过建议改为真实 IP
WHERE config_key = 'ai_external_base_url' AND family_id IS NULL;

-- 3. 如果不存在，插入它 (注意：通常建议在生产环境直接填入真实公网地址)
-- INSERT INTO system_config (config_key, config_value, family_id) 
-- VALUES ('ai_external_base_url', 'http://您的服务器IP:9900', NULL);

-- 💡 建议：
-- 如果您希望前端完全自动识别，可以将数据库中的该值设为 "" (空字符串) 或者不配置它。
-- 我已经修改了 index.html，使其在数据库配置为空时，自动尝试使用 [当前页面域名:9900] 作为地址。
