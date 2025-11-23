# 家宴菜单管理系统

一个现代化的家庭菜单管理系统，包含后台管理系统和微信小程序端，支持菜品管理、订单管理、用户管理等功能。

## 📋 目录

- [项目简介](#项目简介)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [本地开发](#本地开发)
- [生产部署](#生产部署)
- [常见问题](#常见问题)

## 🎯 项目简介

本项目包含三个主要模块：

- **后端服务** (`food_menu`)：基于 Spring Boot 的 RESTful API 服务
- **管理后台** (`food_menu_admin`)：基于 Vue 3 的现代化管理界面
- **微信小程序** (`food_menu_weixin`)：面向C端用户的点餐小程序

### 主要功能

#### 管理后台

- 🍽️ 菜品分类管理
- 📝 菜品信息管理（支持图片上传）
- 📦 订单管理
- 👥 用户管理（管理员 + 小程序用户）
- 🎨 轮播图管理
- 🌓 深色/浅色主题切换
- 📱 响应式设计

#### 微信小程序

- 🏠 首页展示（轮播图、推荐菜品）
- 🍜 菜品浏览与搜索
- � 购物车管理
- 📋 订单下单与查询
- ❤️ 收藏功能
- 👤 个人中心

## �🛠️ 技术栈

### 后端

- Java 17
- Spring Boot 3.x
- MyBatis-Plus
- MySQL 8.0
- 阿里云 OSS（图片存储）
- Knife4j（API 文档）

### 管理后台

- Vue 3
- TypeScript
- Naive UI
- Pinia（状态管理）
- UnoCSS（原子化 CSS）
- Vite

### 微信小程序

- 微信小程序原生开发
- ES6+
- Promise 封装

## 📁 项目结构

```
foodMenuProject/
├── food_menu/              # 后端服务
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
├── food_menu_admin/        # 管理后台
│   ├── src/
│   │   ├── api/
│   │   ├── components/
│   │   ├── layouts/
│   │   ├── store/
│   │   ├── styles/
│   │   └── views/
│   ├── package.json
│   └── vite.config.ts
├── food_menu_weixin/       # 微信小程序
│   ├── pages/
│   ├── utils/
│   ├── app.js
│   ├── app.json
│   └── project.config.json
├── deploy.sh               # 一键部署脚本
└── README.md
```

## 🚀 快速开始

### 环境要求

- **JDK**: 17 或更高版本
- **Node.js**: 18 或更高版本
- **MySQL**: 8.0 或更高版本
- **Maven**: 3.6+ （或使用项目自带的 mvnw）
- **微信开发者工具**: 最新稳定版

### 克隆项目

```bash
git clone <your-repository-url>
cd foodMenuProject
```

## 💻 本地开发

### 1. 数据库初始化

创建数据库并导入表结构：

```sql
CREATE DATABASE IF NOT EXISTS food_menu DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE food_menu;
SOURCE /path/to/schema.sql;
```

### 2. 配置后端

编辑 `food_menu/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/food_menu?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password

aliyun:
  oss:
    endpoint: your_oss_endpoint
    access-key-id: your_access_key_id
    access-key-secret: your_access_key_secret
    bucket-name: your_bucket_name
```

### 3. 启动后端服务

```bash
cd food_menu

# Windows
.\mvnw spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动。

API 文档访问：`http://localhost:8080/doc.html`

### 4. 启动管理后台

```bash
cd food_menu_admin
npm install
npm run dev
```

管理后台将在 `http://localhost:5173` 启动。

### 5. 启动微信小程序

1. **配置 API 地址**

编辑 `food_menu_weixin/utils/request.js`：

```javascript
const BASE_URL = 'http://localhost:8080'; // 本地开发
// const BASE_URL = 'https://your-domain.com/api'; // 生产环境
```

2. **打开微信开发者工具**

- 导入项目：选择 `food_menu_weixin` 目录
- AppID：使用测试号或您的小程序 AppID
- 点击"编译"即可预览

3. **配置合法域名**（生产环境必需）

在微信公众平台 > 开发 > 开发管理 > 开发设置 > 服务器域名中配置：

- request合法域名：`https://your-domain.com`
- uploadFile合法域名：`https://your-oss-domain.com`

### 6. 默认登录账号

**管理后台**：

- 用户名: `admin`
- 密码: `123456`

**微信小程序**：

- 支持手机号验证码登录（测试环境验证码固定为 `1234`）

> ⚠️ **安全提示**：首次登录后请立即修改默认密码！

## 🌐 生产部署

### 部署架构

```
                    ┌─────────────────┐
                    │   用户/管理员    │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │  Nginx (80/443) │
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
    ┌─────────▼────────┐ ┌──▼──────┐ ┌────▼────────┐
    │  管理后台静态资源 │ │ API代理 │ │ 微信小程序   │
    │  /var/www/admin  │ │  /api   │ │  (云端)     │
    └──────────────────┘ └──┬──────┘ └─────────────┘
                            │
                    ┌───────▼────────┐
                    │  后端服务:8080  │
                    └───────┬────────┘
                            │
                    ┌───────▼────────┐
                    │  MySQL 数据库   │
                    └────────────────┘
```

### 方式一：手动部署（完整流程）

#### 步骤 1：准备服务器

1. **购买云服务器**（阿里云/腾讯云/华为云等）
   - 推荐配置：2核4G，40GB硬盘
   - 操作系统：Ubuntu 20.04 LTS

2. **安装必需软件**

```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装 Java 17
sudo apt install openjdk-17-jdk -y
java -version

# 安装 MySQL
sudo apt install mysql-server -y
sudo mysql_secure_installation

# 安装 Nginx
sudo apt install nginx -y

# 安装 Node.js（用于构建前端）
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install nodejs -y
node -v
npm -v
```

#### 步骤 2：部署后端

1. **创建生产配置**

在 `food_menu/src/main/resources/` 创建 `application-prod.yml`：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/food_menu?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_db_user
    password: your_db_password

# 关闭 Swagger（生产环境）
knife4j:
  enable: false
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false

aliyun:
  oss:
    endpoint: your_oss_endpoint
    access-key-id: your_access_key_id
    access-key-secret: your_access_key_secret
    bucket-name: your_bucket_name
```

2. **构建 JAR 包**

```bash
cd food_menu
./mvnw clean package -DskipTests
```

生成的 JAR 包：`target/food_menu-0.0.1-SNAPSHOT.jar`

3. **上传到服务器**

```bash
# 在本地执行
scp target/food_menu-0.0.1-SNAPSHOT.jar user@your-server-ip:/opt/food-menu/
scp src/main/resources/application-prod.yml user@your-server-ip:/opt/food-menu/
```

4. **创建 systemd 服务**

在服务器上创建 `/etc/systemd/system/food-menu.service`：

```ini
[Unit]
Description=Food Menu Backend Service
After=network.target mysql.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/food-menu
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar /opt/food-menu/food_menu-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod --spring.config.additional-location=/opt/food-menu/application-prod.yml
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

5. **启动后端服务**

```bash
sudo systemctl daemon-reload
sudo systemctl enable food-menu
sudo systemctl start food-menu
sudo systemctl status food-menu
```

查看日志：

```bash
sudo journalctl -u food-menu -f
```

#### 步骤 3：部署管理后台

1. **配置生产环境 API 地址**

编辑 `food_menu_admin/src/api/http.ts`：

```typescript
const http = axios.create({
  baseURL: process.env.NODE_ENV === 'production' 
    ? '/api'  // 生产环境通过 Nginx 代理
    : '/api', // 开发环境通过 Vite 代理
  timeout: 10000
});
```

2. **构建生产版本**

```bash
cd food_menu_admin
npm install
npm run build
```

3. **上传到服务器**

```bash
# 在本地执行
scp -r dist/* user@your-server-ip:/var/www/food-menu-admin/
```

或在服务器上：

```bash
sudo mkdir -p /var/www/food-menu-admin
# 将 dist 内容上传到此目录
```

#### 步骤 4：部署微信小程序

1. **配置生产环境 API**

编辑 `food_menu_weixin/utils/request.js`：

```javascript
// 生产环境使用 HTTPS 域名
const BASE_URL = 'https://your-domain.com/api';
```

2. **配置小程序 AppID**

编辑 `food_menu_weixin/project.config.json`：

```json
{
  "appid": "your-miniprogram-appid",
  "projectname": "food_menu_weixin"
}
```

3. **配置服务器域名**

登录[微信公众平台](https://mp.weixin.qq.com/)：

- 开发 > 开发管理 > 开发设置 > 服务器域名
- 配置以下域名：
  - **request合法域名**: `https://your-domain.com`
  - **uploadFile合法域名**: `https://your-oss-endpoint.com`
  - **downloadFile合法域名**: `https://your-oss-endpoint.com`

4. **上传小程序代码**

使用微信开发者工具：

- 打开项目 `food_menu_weixin`
- 点击右上角"上传"
- 填写版本号和项目备注
- 上传成功后，登录微信公众平台提交审核

5. **提交审核**

- 登录微信公众平台
- 管理 > 版本管理
- 选择开发版本，点击"提交审核"
- 填写审核信息（功能介绍、测试账号等）
- 等待审核通过后发布

#### 步骤 5：配置 Nginx

1. **创建 Nginx 配置**

创建 `/etc/nginx/sites-available/food-menu`：

```nginx
upstream backend_api {
    server 127.0.0.1:8080;
}

server {
    listen 80;
    server_name your-domain.com;  # 替换为您的域名
    
    # 管理后台静态资源
    location / {
        root /var/www/food-menu-admin;
        try_files $uri $uri/ /index.html;
        
        # 静态资源缓存
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }
    
    # API 代理（管理后台 + 小程序共用）
    location /api/ {
        proxy_pass http://backend_api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # 文件上传大小限制
        client_max_body_size 20M;
    }
    
    # Gzip 压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript application/javascript application/xml+rss application/json;
}
```

2. **启用站点**

```bash
sudo ln -s /etc/nginx/sites-available/food-menu /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl reload nginx
```

3. **配置 HTTPS（必需，小程序要求）**

```bash
# 安装 Certbot
sudo apt install certbot python3-certbot-nginx -y

# 获取免费 SSL 证书
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo certbot renew --dry-run
```

#### 步骤 6：配置防火墙

```bash
# 允许 HTTP 和 HTTPS
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 22/tcp  # SSH
sudo ufw enable
sudo ufw status
```

### 方式二：使用一键部署脚本

我们提供了自动化部署脚本，简化部署流程。

#### 1. 配置部署参数

编辑 `deploy.sh`，修改以下变量：

```bash
DB_PASSWORD="your_db_password"
OSS_ENDPOINT="your_oss_endpoint"
OSS_ACCESS_KEY="your_access_key"
OSS_SECRET_KEY="your_secret_key"
DOMAIN="your-domain.com"
```

#### 2. 执行部署

```bash
# 上传脚本到服务器
scp deploy.sh user@your-server:/tmp/

# 在服务器上执行
ssh user@your-server
cd /tmp
chmod +x deploy.sh
sudo ./deploy.sh
```

脚本将自动完成：

- ✅ 安装所有依赖
- ✅ 配置数据库
- ✅ 部署后端服务
- ✅ 配置 Nginx
- ✅ 配置 SSL 证书

#### 3. 手动上传代码

脚本执行完成后，需要手动上传：

```bash
# 上传后端 JAR
scp target/food_menu-0.0.1-SNAPSHOT.jar user@your-server:/opt/food-menu/

# 上传管理后台
scp -r food_menu_admin/dist/* user@your-server:/var/www/food-menu-admin/

# 启动后端服务
ssh user@your-server
sudo systemctl start food-menu
```

### 方式三：使用宝塔面板（适合新手）

1. **安装宝塔面板**

```bash
wget -O install.sh https://download.bt.cn/install/install-ubuntu_6.0.sh && sudo bash install.sh
```

2. **登录宝塔面板**

访问 `http://your-server-ip:8888`，使用安装时显示的账号密码登录。

3. **安装软件**

在宝塔面板中安装：

- Nginx
- MySQL 8.0
- Java 项目管理器

4. **部署后端**

- 上传 JAR 包到 `/www/wwwroot/food-menu/`
- 在 Java 项目管理器中添加项目
- 配置启动参数：`--spring.profiles.active=prod`
- 启动项目

5. **部署管理后台**

- 上传 `dist` 目录到 `/www/wwwroot/food-menu-admin/`
- 在网站管理中添加站点
- 配置反向代理：`/api` → `http://127.0.0.1:8080`

6. **配置 SSL**

- 在网站设置中申请 Let's Encrypt 证书
- 开启强制 HTTPS

## 📦 部署检查清单

部署完成后，请逐项检查：

### 后端检查

- [ ] 服务启动成功：`sudo systemctl status food-menu`
- [ ] 数据库连接正常
- [ ] API 接口可访问：`curl http://localhost:8080/api/health`
- [ ] 日志无错误：`sudo journalctl -u food-menu -n 50`

### 管理后台检查

- [ ] 页面可正常访问：`https://your-domain.com`
- [ ] 静态资源加载正常（无 404）
- [ ] 登录功能正常
- [ ] API 请求成功（检查浏览器控制台）
- [ ] 图片上传功能正常

### 微信小程序检查

- [ ] 服务器域名已配置
- [ ] HTTPS 证书有效
- [ ] 小程序可正常登录
- [ ] 菜品列表加载正常
- [ ] 下单功能正常
- [ ] 图片显示正常

### 安全检查

- [ ] 修改默认管理员密码
- [ ] 数据库使用强密码
- [ ] 生产环境已关闭 Swagger
- [ ] 防火墙已配置
- [ ] HTTPS 已启用
- [ ] OSS 访问权限正确配置

## 🔧 运维管理

### 查看服务状态

```bash
# 后端服务状态
sudo systemctl status food-menu

# Nginx 状态
sudo systemctl status nginx

# MySQL 状态
sudo systemctl status mysql
```

### 查看日志

```bash
# 后端日志（实时）
sudo journalctl -u food-menu -f

# Nginx 访问日志
sudo tail -f /var/log/nginx/access.log

# Nginx 错误日志
sudo tail -f /var/log/nginx/error.log
```

### 重启服务

```bash
# 重启后端
sudo systemctl restart food-menu

# 重启 Nginx
sudo systemctl restart nginx

# 重启 MySQL
sudo systemctl restart mysql
```

### 数据库备份

创建备份脚本 `/opt/backup/backup-db.sh`：

```bash
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/opt/backup"
DB_NAME="food_menu"
DB_USER="root"
DB_PASS="your_password"

# 创建备份
mysqldump -u $DB_USER -p$DB_PASS $DB_NAME > $BACKUP_DIR/food_menu_$DATE.sql

# 压缩备份
gzip $BACKUP_DIR/food_menu_$DATE.sql

# 删除7天前的备份
find $BACKUP_DIR -name "food_menu_*.sql.gz" -mtime +7 -delete

echo "Backup completed: food_menu_$DATE.sql.gz"
```

设置定时任务：

```bash
chmod +x /opt/backup/backup-db.sh
crontab -e

# 添加以下行（每天凌晨2点备份）
0 2 * * * /opt/backup/backup-db.sh >> /var/log/backup.log 2>&1
```

### 更新部署

#### 更新后端

```bash
# 1. 构建新的 JAR 包
cd food_menu
./mvnw clean package -DskipTests

# 2. 上传到服务器
scp target/food_menu-0.0.1-SNAPSHOT.jar user@your-server:/opt/food-menu/

# 3. 重启服务
sudo systemctl restart food-menu
```

#### 更新管理后台

```bash
# 1. 构建新版本
cd food_menu_admin
npm run build

# 2. 上传到服务器
scp -r dist/* user@your-server:/var/www/food-menu-admin/

# 3. 清除浏览器缓存或使用 Ctrl+F5 强制刷新
```

#### 更新微信小程序

```bash
# 1. 修改代码后，在微信开发者工具中点击"上传"
# 2. 登录微信公众平台提交审核
# 3. 审核通过后发布新版本
```

## ❓ 常见问题

### 1. 后端无法启动

**问题**：`sudo systemctl status food-menu` 显示 failed

**解决方案**：

```bash
# 查看详细错误日志
sudo journalctl -u food-menu -n 100

# 常见原因：
# - 端口被占用：sudo netstat -tulpn | grep 8080
# - 数据库连接失败：检查配置文件中的数据库信息
# - Java 版本不对：java -version（需要 17+）
```

### 2. 管理后台无法访问后端 API

**问题**：浏览器控制台显示 CORS 错误或 404

**解决方案**：

```bash
# 检查 Nginx 配置
sudo nginx -t

# 检查后端服务是否运行
curl http://localhost:8080/api/health

# 查看 Nginx 错误日志
sudo tail -f /var/log/nginx/error.log
```

### 3. 小程序无法连接后端

**问题**：小程序提示"网络请求失败"

**解决方案**：

- 检查服务器域名是否已在微信公众平台配置
- 确认域名使用 HTTPS（小程序要求）
- 检查 SSL 证书是否有效
- 在微信开发者工具中查看详细错误信息

### 4. 图片上传失败

**问题**：上传图片时报错

**解决方案**：

- 检查 OSS 配置是否正确
- 检查 Nginx `client_max_body_size` 设置（默认 20M）
- 查看后端日志中的详细错误信息
- 确认 OSS Bucket 权限配置正确

### 5. 数据库连接失败

**问题**：后端日志显示数据库连接错误

**解决方案**：

```bash
# 检查 MySQL 是否运行
sudo systemctl status mysql

# 测试数据库连接
mysql -u root -p

# 检查用户权限
mysql> SHOW GRANTS FOR 'your_user'@'localhost';

# 如果需要创建用户
mysql> CREATE USER 'food_menu'@'localhost' IDENTIFIED BY 'password';
mysql> GRANT ALL PRIVILEGES ON food_menu.* TO 'food_menu'@'localhost';
mysql> FLUSH PRIVILEGES;
```

### 6. 小程序审核被拒

**常见原因**：

- 缺少用户协议和隐私政策
- 功能描述不清晰
- 测试账号无法正常使用
- 缺少必要的资质证明

**解决方案**：

- 完善用户协议和隐私政策页面
- 提供详细的功能说明和操作步骤
- 确保测试账号可以正常登录和使用
- 根据审核意见补充相关资质

## 📞 技术支持

如遇到问题，请提供以下信息：

1. 操作系统版本：`lsb_release -a`
2. Java 版本：`java -version`
3. 错误日志：`sudo journalctl -u food-menu -n 100`
4. Nginx 配置：`sudo nginx -t`

## 📄 许可证

本项目仅供学习和研究使用。

## 🙏 致谢

感谢所有开源项目的贡献者！
