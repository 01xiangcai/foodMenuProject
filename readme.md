# 家宴菜单管理系统

一个现代化的多家庭菜单管理系统，支持数据隔离、多端访问，包含后台管理系统和小程序端，提供完整的菜品管理、订单管理、用户管理等功能。

## 📋 目录

- [项目简介](#项目简介)
- [技术架构](#技术架构)
- [功能特性](#功能特性)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [本地开发](#本地开发)
- [生产部署](#生产部署)
- [常见问题](#常见问题)

---

## 🎯 项目简介

**家宴菜单管理系统** 是一个完整的家庭菜单管理解决方案，采用前后端分离架构，支持多家庭数据隔离。

### 三端架构

- **后端服务** (`food_menu`)：基于 Spring Boot 3 的 RESTful API 服务
- **管理后台** (`food_menu_admin`)：基于 Vue 3 + TypeScript 的现代化管理界面
- **小程序端** (`food_menu_uniapp`)：基于 Uni-app 的跨平台小程序（支持微信小程序、H5）

### 核心特性

✅ **多家庭数据隔离** - 基于 family_id 的多租户架构，数据完全隔离  
✅ **双端用户系统** - 管理员(User) + 小程序用户(WxUser)  
✅ **灵活存储方案** - 支持阿里云 OSS 和本地存储无缝切换  
✅ **完善权限控制** - 普通用户、家庭管理员、超级管理员三级权限  
✅ **JWT 认证** - 安全的 Token 认证机制  
✅ **友好错误提示** - 全局异常处理，中文错误信息  

---

## 🏗️ 技术架构

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 3.3.5 | 核心框架 |
| MyBatis-Plus | 3.5.7 | ORM 框架 |
| MySQL | 8.0+ | 数据库 |
| JWT | 0.11.5 | Token 认证 |
| Knife4j | 4.5.0 | API 文档 |
| 阿里云 OSS | 3.17.2 | 图片存储 |
| Spring Security Crypto | - | 密码加密 |

### 管理后台技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5+ | 前端框架 |
| TypeScript | 5.6+ | 开发语言 |
| Naive UI | 2.38+ | UI 组件库 |
| Pinia | 2.2+ | 状态管理 |
| UnoCSS | 0.63+ | 原子化 CSS |
| ECharts | 5.5+ | 数据可视化 |
| Vite | 5.4+ | 构建工具 |
| Vue Router | 4.4+ | 路由管理 |

### 小程序技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Uni-app | 3.0 | 跨平台框架 |
| Vue | 3.4+ | 前端框架 |
| Pinia | 2.1+ | 状态管理 |
| Sass | 1.77+ | CSS 预处理器 |
| Vite | 5.2+ | 构建工具 |

---

## 🌟 功能特性

### 管理后台功能

#### 📊 数据看板
- 订单统计（今日/本周/本月）
- 菜品统计（总数、热门菜品）
- 用户统计
- 订单流可视化
- 口味网络图
- 菜品评分趋势

#### 🍽️ 菜品管理
- 菜品 CRUD（创建、查看、更新、删除）
- 菜品分类管理
- 多图片上传
- 标签管理（低油、高蛋白等）
- 口味配置
- 价格、描述、卡路里管理
- 上下架控制

#### 📦 订单管理
- 订单列表查看
- 订单状态管理（待接单、准备中、配送中、已完成）
- 订单详情查看
- 订单备注修改
- 订单时间轴

#### 👥 用户管理
- 统一用户管理（管理员 + 小程序用户）
- 用户状态控制（启用/禁用）
- 角色管理（普通用户、家庭管理员、超级管理员）
- 密码重置
- 用户搜索、筛选

#### 🎨 轮播图管理
- 轮播图 CRUD
- 图片上传
- 排序管理
- 状态控制

#### 🏷️ 标签管理
- 标签 CRUD
- 图标配置（emoji）
- 标签分类

#### 🏠 家庭管理
- 家庭创建
- 邀请码生成
- 家庭成员管理
- 数据隔离

#### 📸 图片迁移工具
- OSS ↔ 本地存储双向迁移
- 批量迁移
- 进度显示

#### ⚙️ 系统设置
- 存储方式切换（OSS/本地）
- 系统配置管理

#### 🌓 主题切换
- 深色/浅色主题
- 响应式设计

### 小程序端功能

#### 🏠 首页
- 轮播图展示
- 推荐菜品
- 快速导航
- 主题切换（6种预设主题）

#### 📋 菜单浏览
- 按分类浏览菜品
- 菜品搜索
- 按标签筛选
- 菜品详情查看
- 多图片查看

#### 🛒 购物车
- 添加/删除菜品
- 数量调整
- 价格计算
- 一键清空

#### 📦 订单管理
- 确认订单
- 订单提交
- 我的订单列表
- 订单详情查看
- 订单备注修改
- 订单取消

#### ❤️ 收藏功能
- 收藏喜爱的菜品
- 收藏列表查看
- 取消收藏

#### 💬 评论功能
- 菜品评论
- 查看评论列表
- 评论回复（支持二级评论）

#### 🎲 随机选菜
- 随机推荐菜品
- 按标签筛选（如：低油、快手菜）
- 按分类筛选
- 解决"不知道吃什么"的问题

#### 👤 个人中心
- 个人信息展示
- 个人信息编辑（昵称、头像、手机号、性别）
- 头像上传（每日限3次）
- 家庭信息查看
- 加入家庭（邀请码）
- 主题切换

#### 🔐 认证功能
- 用户名/手机号 + 密码登录
- 手机号 + 验证码登录
- 用户注册
- 自动识别登录方式

---

## 📁 项目结构

```
foodMenuProject/
├── food_menu/                    # 后端服务
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/yao/food_menu/
│   │   │   │   ├── common/          # 公共模块
│   │   │   │   │   ├── config/      # 配置类（MyBatis-Plus、Knife4j、存储等）
│   │   │   │   │   ├── context/     # 上下文（家庭上下文）
│   │   │   │   │   ├── exception/   # 全局异常处理
│   │   │   │   │   ├── interceptor/ # 拦截器（JWT、数据隔离）
│   │   │   │   │   └── util/        # 工具类
│   │   │   │   ├── controller/      # 控制器（16个）
│   │   │   │   ├── dto/             # 数据传输对象（14个）
│   │   │   │   ├── entity/          # 实体类（14个）
│   │   │   │   ├── mapper/          # Mapper 接口（14个）
│   │   │   │   ├── service/         # 服务接口及实现
│   │   │   │   │   ├── impl/        # 服务实现类（17个）
│   │   │   │   │   └── storage/     # 存储策略（本地/OSS）
│   │   │   │   └── task/            # 定时任务
│   │   │   └── resources/
│   │   │       ├── application.yml          # 开发环境配置
│   │   │       ├── application-prod.yml     # 生产环境配置
│   │   │       └── sql/
│   │   │           └── food_menu.sql        # 数据库表结构
│   │   └── test/                    # 测试
│   └── pom.xml
│
├── food_menu_admin/              # 管理后台
│   ├── src/
│   │   ├── api/                 # API 接口
│   │   │   ├── http.ts          # Axios 配置
│   │   │   └── modules.ts       # API 模块
│   │   ├── components/          # 公共组件
│   │   │   ├── base/
│   │   │   │   └── EChart.vue   # ECharts 封装
│   │   │   ├── FlavorNetwork.vue    # 口味网络图
│   │   │   ├── OrderStream.vue      # 订单流
│   │   │   ├── OrderTimeline.vue    # 订单时间轴
│   │   │   ├── StarDishes.vue       # 明星菜品
│   │   │   ├── StatCard.vue         # 统计卡片
│   │   │   └── ThemeToggle.vue      # 主题切换
│   │   ├── layouts/             # 布局组件
│   │   │   └── DefaultLayout.vue
│   │   ├── router/              # 路由配置
│   │   ├── store/               # 状态管理
│   │   │   ├── theme.ts         # 主题状态
│   │   │   └── useUserStore.ts  # 用户状态
│   │   ├── styles/              # 全局样式
│   │   ├── views/               # 页面视图
│   │   │   ├── auth/            # 登录
│   │   │   ├── dashboard/       # 数据看板
│   │   │   ├── dishes/          # 菜品管理
│   │   │   ├── orders/          # 订单管理
│   │   │   ├── users/           # 用户管理
│   │   │   ├── banners/         # 轮播图管理
│   │   │   ├── tags/            # 标签管理
│   │   │   ├── families/        # 家庭管理
│   │   │   ├── migration/       # 图片迁移
│   │   │   └── settings/        # 系统设置
│   │   └── main.ts
│   ├── package.json
│   ├── vite.config.ts
│   └── uno.config.ts
│
├── food_menu_uniapp/             # 小程序端（Uni-app）
│   ├── src/
│   │   ├── api/                 # API 接口
│   │   │   ├── comment.js       # 评论 API
│   │   │   └── index.js         # 通用 API
│   │   ├── components/          # 公共组件
│   │   │   ├── CartPopup.vue    # 购物车弹窗
│   │   │   ├── CommentInput.vue # 评论输入
│   │   │   └── CommentList.vue  # 评论列表
│   │   ├── pages/               # 页面
│   │   │   ├── index/           # 首页
│   │   │   ├── menu/            # 菜单页
│   │   │   ├── cart/            # 购物车
│   │   │   ├── order/           # 订单
│   │   │   │   ├── confirm.vue      # 确认订单
│   │   │   │   ├── list.vue         # 我的订单
│   │   │   │   ├── detail.vue       # 订单详情
│   │   │   │   └── admin-list.vue   # 管理员订单列表
│   │   │   ├── detail/          # 菜品详情
│   │   │   ├── favorites/       # 我的收藏
│   │   │   ├── random/          # 随机选菜
│   │   │   ├── profile/         # 个人中心
│   │   │   │   ├── profile.vue      # 个人中心首页
│   │   │   │   └── personal-info.vue # 个人信息编辑
│   │   │   ├── login/           # 登录
│   │   │   ├── register/        # 注册
│   │   │   └── family/          # 家庭
│   │   │       └── join.vue         # 加入家庭
│   │   ├── stores/              # 状态管理
│   │   │   ├── cart.js          # 购物车状态
│   │   │   └── theme.js         # 主题状态
│   │   ├── utils/               # 工具类
│   │   │   ├── request.js       # 请求封装
│   │   │   ├── image.js         # 图片工具
│   │   │   └── theme.js         # 主题工具
│   │   ├── styles/              # 全局样式
│   │   ├── static/              # 静态资源
│   │   ├── pages.json           # 页面配置
│   │   └── manifest.json        # 应用配置
│   ├── package.json
│   └── vite.config.js
│
├── deploy.sh                     # 一键部署脚本
├── nginx-food-menu.conf          # Nginx 配置示例
└── README.md                     # 项目说明
```

---

## 💾 数据库设计

### 核心数据表

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| **user** | 管理员用户 | username, password, family_id, role |
| **wx_user** | 小程序用户 | username, phone, family_id, role, avatar |
| **family** | 家庭信息 | name, invite_code, description |
| **dish** | 菜品 | name, category_id, price, tags, image, family_id |
| **category** | 菜品分类 | name, type, sort, family_id |
| **dish_tag** | 菜品标签 | name, icon, family_id |
| **orders** | 订单 | user_id, total_amount, status, family_id |
| **order_items** | 订单项 | order_id, dish_id, quantity, price |
| **dish_comment** | 菜品评论 | dish_id, wx_user_id, parent_id, content |
| **dish_favorite** | 收藏 | wx_user_id, dish_id |
| **dish_statistics** | 菜品统计 | dish_id, view_count, favorite_count, order_count |
| **banner** | 轮播图 | image, title, sort, family_id |
| **system_config** | 系统配置 | config_key, config_value |

### 数据隔离机制

- 使用 `family_id` 字段实现多家庭数据隔离
- MyBatis-Plus 拦截器自动添加 WHERE 条件
- 超级管理员 (role=2) 可跨家庭查看数据

### 权限系统

| 角色 | role 值 | 权限范围 |
|------|---------|---------|
| 普通用户 | 0 | 仅查看自己家庭数据 |
| 家庭管理员 | 1 | 管理自己家庭数据 |
| 超级管理员 | 2 | 管理所有家庭数据 |

---

## 🚀 快速开始

### 环境要求

- **JDK**: 17 或更高版本
- **Node.js**: 18 或更高版本
- **MySQL**: 8.0 或更高版本
- **Maven**: 3.6+ （或使用项目自带的 mvnw）

### 克隆项目

```bash
git clone <your-repository-url>
cd foodMenuProject
```

---

## 💻 本地开发

### 1. 数据库初始化

创建数据库并导入表结构：

```sql
CREATE DATABASE IF NOT EXISTS food_menu DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE food_menu;
SOURCE food_menu/src/main/resources/sql/food_menu.sql;
```

### 2. 配置后端

编辑 `food_menu/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/food_menu?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password

# 图片存储配置（二选一）
file-storage:
  type: local  # 或 oss

# 本地存储配置
local-storage:
  base-path: D:/food-menu-storage  # Windows
  # base-path: /opt/food-menu-storage  # Linux
  url-prefix: http://localhost:8080/storage

# OSS 存储配置（如果使用 OSS）
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
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动。

**API 文档访问**：`http://localhost:8080/doc.html`

### 4. 启动管理后台

```bash
cd food_menu_admin
npm install
npm run dev
```

管理后台将在 `http://localhost:5173` 启动。

**默认登录账号**：
- 用户名: `admin`
- 密码: `123456`

> ⚠️ **安全提示**：首次登录后请立即修改默认密码！

### 5. 启动小程序

#### 方式一：微信小程序

1. **配置 API 地址**

编辑 `food_menu_uniapp/src/utils/request.js`：

```javascript
// 开发环境
const BASE_URL = 'http://localhost:8080';
```

2. **安装依赖**

```bash
cd food_menu_uniapp
npm install
```

3. **编译**

```bash
# 编译到微信小程序
npm run dev:mp-weixin
```

4. **打开微信开发者工具**

- 导入项目：选择 `food_menu_uniapp/dist/dev/mp-weixin` 目录
- AppID：使用测试号或您的小程序 AppID
- 点击"编译"即可预览

#### 方式二：H5（浏览器预览）

```bash
cd food_menu_uniapp
npm run dev:h5
```

访问 `http://localhost:5174`

**默认登录方式**：
- 用户名/手机号 + 密码登录
- 手机号 + 验证码登录（测试环境验证码固定为 `1234`）

---

## 🌐 生产部署

### 部署架构

```
用户/管理员
    ↓
Nginx (80/443)
    ↓
    ├─→ /           → 管理后台静态文件 (/var/www/food-menu-admin)
    ├─→ /api        → 后端API代理 (→ localhost:8080)
    └─→ 小程序       → 直接访问 API (HTTPS)
            ↓
       Spring Boot (8080)
            ↓
       MySQL (3306)
            ↓
   阿里云 OSS / 本地存储
```

### 快速部署步骤

#### 1. 准备服务器

- 推荐配置：2核4G，40GB硬盘
- 操作系统：Ubuntu 20.04 LTS / CentOS 7+

安装必需软件：

```bash
# Ubuntu
sudo apt update && sudo apt upgrade -y
sudo apt install openjdk-17-jdk nginx mysql-server -y

# CentOS
sudo yum update -y
sudo yum install java-17-openjdk nginx mysql-server -y
```

#### 2. 部署后端

```bash
# 构建 JAR 包
cd food_menu
./mvnw clean package -DskipTests

# 上传到服务器
scp target/food_menu-0.0.1-SNAPSHOT.jar user@your-server:/opt/food-menu/

# 创建 systemd 服务
sudo nano /etc/systemd/system/food-menu.service
```

服务配置：

```ini
[Unit]
Description=Food Menu Backend Service
After=network.target mysql.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/food-menu
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar /opt/food-menu/food_menu-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启动服务：

```bash
sudo systemctl daemon-reload
sudo systemctl enable food-menu
sudo systemctl start food-menu
sudo systemctl status food-menu
```

#### 3. 部署管理后台

```bash
# 构建
cd food_menu_admin
npm install
npm run build

# 上传到服务器
scp -r dist/* user@your-server:/var/www/food-menu-admin/
```

#### 4. 配置 Nginx

创建 `/etc/nginx/sites-available/food-menu`：

```nginx
upstream backend_api {
    server 127.0.0.1:8080;
}

server {
    listen 80;
    server_name your-domain.com;
    
    # 管理后台静态资源
    location / {
        root /var/www/food-menu-admin;
        try_files $uri $uri/ /index.html;
        
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }
    
    # API 代理
    location /api/ {
        proxy_pass http://backend_api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        
        client_max_body_size 20M;
    }
    
    # Gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript;
}
```

启用站点：

```bash
sudo ln -s /etc/nginx/sites-available/food-menu /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

#### 5. 配置 HTTPS（小程序必需）

```bash
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d your-domain.com
```

#### 6. 部署小程序

1. 修改 API 地址为生产环境：

```javascript
// food_menu_uniapp/src/utils/request.js
const BASE_URL = 'https://your-domain.com/api';
```

2. 在微信公众平台配置服务器域名：
   - request合法域名: `https://your-domain.com`
   - uploadFile合法域名: `https://your-oss-endpoint.com`（如果使用OSS）

3. 使用微信开发者工具上传代码并提交审核

---

## 🔧 运维管理

### 查看服务状态

```bash
# 后端服务
sudo systemctl status food-menu

# 查看日志
sudo journalctl -u food-menu -f
```

### 重启服务

```bash
# 重启后端
sudo systemctl restart food-menu

# 重启 Nginx
sudo systemctl restart nginx
```

### 数据库备份

```bash
# 手动备份
mysqldump -u root -p food_menu > backup_$(date +%Y%m%d).sql

# 设置定时备份
crontab -e
# 添加：每天凌晨2点备份
0 2 * * * mysqldump -u root -p'your_password' food_menu | gzip > /opt/backup/food_menu_$(date +\%Y\%m\%d).sql.gz
```

### 更新部署

```bash
# 更新后端
cd food_menu
./mvnw clean package -DskipTests
scp target/*.jar user@server:/opt/food-menu/
ssh user@server "sudo systemctl restart food-menu"

# 更新管理后台
cd food_menu_admin
npm run build
scp -r dist/* user@server:/var/www/food-menu-admin/
```

---

## ❓ 常见问题

### 1. 后端无法启动

**问题**：`sudo systemctl status food-menu` 显示 failed

**解决方案**：

```bash
# 查看详细日志
sudo journalctl -u food-menu -n 100

# 常见原因：
# - 端口被占用：sudo netstat -tulpn | grep 8080
# - 数据库连接失败：检查 application-prod.yml 配置
# - Java 版本不对：java -version（需要 17+）
```

### 2. 图片上传失败

**问题**：上传图片时报错

**解决方案**：

- 检查 `file-storage.type` 配置（local 或 oss）
- 本地存储：确保 `local-storage.base-path` 目录存在且有写权限
- OSS 存储：检查 OSS 配置是否正确
- 检查 Nginx `client_max_body_size` 设置（默认 20M）

### 3. 小程序无法连接后端

**问题**：小程序提示"网络请求失败"

**解决方案**：

- 确认域名使用 HTTPS（小程序要求）
- 检查服务器域名是否已在微信公众平台配置
- 检查 SSL 证书是否有效
- 在微信开发者工具中查看详细错误信息

### 4. 数据库连接失败

**问题**：后端日志显示数据库连接错误

**解决方案**：

```bash
# 检查 MySQL 是否运行
sudo systemctl status mysql

# 测试连接
mysql -u root -p

# 检查用户权限
mysql> SHOW GRANTS FOR 'your_user'@'localhost';

# 创建用户（如需要）
mysql> CREATE USER 'food_menu'@'localhost' IDENTIFIED BY 'password';
mysql> GRANT ALL PRIVILEGES ON food_menu.* TO 'food_menu'@'localhost';
mysql> FLUSH PRIVILEGES;
```

### 5. 管理后台无法访问 API

**问题**：浏览器控制台显示 CORS 错误或 404

**解决方案**：

```bash
# 检查 Nginx 配置
sudo nginx -t

# 检查后端服务
curl http://localhost:8080/api/health

# 查看 Nginx 错误日志
sudo tail -f /var/log/nginx/error.log
```

---


---

## 🎯 项目亮点

1. **多租户架构** - 支持多家庭数据隔离，适合 SaaS 化部署
2. **前后端分离** - 三端独立开发部署，职责清晰
3. **灵活存储方案** - OSS/本地存储可无缝切换
4. **完善权限控制** - 三级权限体系，安全可靠
5. **现代化技术栈** - Spring Boot 3 + Vue 3 + Uni-app
6. **代码规范** - 统一异常处理、日志记录、中文注释
7. **可扩展性强** - 模块化设计，易于扩展新功能

---

