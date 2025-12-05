# 安全加固说明

## ✅ 已完成的安全措施

### 1. 敏感信息加密管理

- ✅ 引入Jasypt配置加密库
- ✅ JWT密钥从代码移至配置文件
- ✅ 支持配置文件敏感信息加密
- ✅ 环境变量管理加密密钥
- ✅ 生产环境配置加密模板

**详细说明：** 请查看 `敏感信息管理说明.md`

### 2. JWT安全

- ✅ JWT密钥可配置化
- ✅ Token有效期配置
- ✅ Token验证机制
- ✅ 用户信息上下文隔离

**配置位置：**
```yaml
jwt:
  secret: ENC(加密后的密钥)
  expiration-time: 604800000  # 7天
```

---

## 🔄 待完善的安全措施

### 1. Redis集成（高优先级）

**当前问题：**
- 验证码存储使用内存HashMap
- 集群环境无法共享会话

**改进方案：**
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### 2. 接口限流（高优先级）

**当前问题：**
- 无接口访问频率限制
- 易受暴力破解和DDoS攻击

**改进方案：**
- 引入Spring Cloud Gateway + Redis限流
- 或使用Sentinel
- 登录接口：5次/分钟/IP
- 验证码接口：10次/小时/手机号

### 3. 短信服务集成（中优先级）

**当前问题：**
- 验证码固定为1234（测试用）

**改进方案：**
- 接入阿里云短信服务
- 接入腾讯云短信服务

### 4. HTTPS配置（生产必需）

**当前问题：**
- 开发环境使用HTTP

**改进方案：**
```bash
# Nginx配置SSL
server {
    listen 443 ssl;
    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;
}
```

### 5. SQL注入防护验证

**当前状态：**
- 已使用MyBatis-Plus（有基础防护）
- 需要代码审计确认

**改进方案：**
- 代码审计检查原生SQL
- 禁止字符串拼接SQL
- 使用参数化查询

### 6. XSS防护

**当前状态：**
- 前端需要输入过滤

**改进方案：**
```java
// 后端输入验证
@NotBlank(message = "评论内容不能为空")
@Size(max = 500, message = "评论内容不能超过500字符")
private String content;
```

### 7. CSRF防护

**当前状态：**
- JWT Token机制有一定防护
- 需要添加Token刷新机制

**改进方案：**
- 实现Token刷新机制
- 添加请求来源验证

---

## 🔒 生产环境部署检查清单

### 启动前检查

- [ ] 已设置 `JASYPT_ENCRYPTOR_PASSWORD` 环境变量
- [ ] 配置文件中敏感信息已加密
- [ ] JWT密钥已更换为生产环境密钥
- [ ] 数据库密码为强密码
- [ ] 关闭Swagger文档接口
- [ ] 配置日志级别为WARN或ERROR
- [ ] 配置HTTPS证书
- [ ] 配置防火墙规则
- [ ] 数据库仅允许应用服务器访问
- [ ] OSS配置访问白名单

### 启动命令示例

```bash
# 设置环境变量
export JASYPT_ENCRYPTOR_PASSWORD=你的加密密钥
export SPRING_PROFILES_ACTIVE=prod

# 启动应用
java -Xms512m -Xmx1024m \
  -jar food_menu-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod
```

### Systemd服务配置

```ini
[Unit]
Description=Food Menu Backend Service
After=network.target mysql.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/food-menu
Environment="JASYPT_ENCRYPTOR_PASSWORD=你的加密密钥"
Environment="SPRING_PROFILES_ACTIVE=prod"
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar /opt/food-menu/food_menu-0.0.1-SNAPSHOT.jar
Restart=on-failure
RestartSec=10

# 安全加固
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=/opt/food-menu/logs

[Install]
WantedBy=multi-user.target
```

---

## 📋 安全审计日志

| 日期 | 措施 | 负责人 | 状态 |
|------|------|--------|------|
| 2024-12-05 | 敏感信息加密管理 | - | ✅ 完成 |
| - | Redis集成 | - | ⏳ 待完成 |
| - | 接口限流 | - | ⏳ 待完成 |
| - | 短信服务集成 | - | ⏳ 待完成 |

---

## 🚨 应急响应

### 密钥泄露处理流程

1. **立即行动：**
   - 禁用泄露的密钥
   - 生成新密钥
   - 重新加密所有敏感信息

2. **影响评估：**
   - 检查访问日志
   - 评估数据泄露范围
   - 通知相关用户

3. **恢复措施：**
   - 更新所有配置
   - 重启所有服务
   - 强制用户重新登录

### 安全事件报告

发现安全漏洞或异常访问，请立即联系：
- **安全负责人：** [姓名/联系方式]
- **技术负责人：** [姓名/联系方式]

---

**最后更新：** 2024-12-05

