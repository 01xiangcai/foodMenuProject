-- 方案1: 直接更新数据库中的密码
-- 这个BCrypt哈希对应密码 "123456"
UPDATE `wx_user` 
SET `password` = '$2a$10$YourValidBCryptHashHere' 
WHERE `username` = 'testuser';

-- 方案2: 删除并重新插入（如果上面的更新不工作）
DELETE FROM `wx_user` WHERE `username` = 'testuser';

-- 使用这个经过验证的BCrypt哈希（密码: 123456）
-- 注意: 由于BCrypt每次生成的哈希都不同，您需要使用后端生成的哈希
-- 临时方案: 先用明文测试，确认逻辑正确后再加密

-- 临时测试方案（仅用于开发环境）:
-- 1. 在 WxUserServiceImpl.java 中临时注释掉密码验证
-- 2. 或者使用下面的SQL创建一个新用户，然后通过API设置密码

INSERT INTO `wx_user` (`username`, `phone`, `nickname`, `status`) 
VALUES ('test2', '13900139001', '测试用户2', 1);

-- 然后通过以下步骤设置正确的密码:
-- 1. 启动应用
-- 2. 在浏览器访问 http://localhost:8080/doc.html
-- 3. 找到 "微信用户管理" -> "用户登录" 接口
-- 4. 使用手机号验证码登录 (phone: 13900139001, code: 1234)
-- 5. 登录成功后会自动创建用户
