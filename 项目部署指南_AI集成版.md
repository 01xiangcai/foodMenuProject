# 餐饮管理系统项目部署指南 (AI 集成版)

本指南针对集成了 AI 服务（如智能菜谱生成、AI 客服等）后的项目提供完整的部署流程说明。

## 1. 总体架构
- **后端 (Java)**: Spring Boot 3.3.5 / Maven / MySQL 8.0 / JDK 17
- **管理后台 (Vue)**: Vite / Vue 3 / Naive UI
- **小程序 (UniApp)**: UniApp / 微信小程序环境

---

## 2. 后端部署 (food_menu)

### 2.1 环境准备
- **JDK**: 17 或以上版本。
- **MySQL**: 8.0+，导入 `food_menu.sql`（如果涉及到数据库变更）。
- **目录准备**: 创建本地图片存储目录（例如 `E:/uploads/food-menu` 或 Linux 下的 `/var/www/uploads/food-menu`）。

### 2.2 核心配置 (application.yml)
在生产环境部署时，建议通过环境变量或启动参数覆盖以下配置：

#### AI 服务配置
项目支持多种 AI 提供商，推荐使用 **Silicon Flow (硅基流动)**：
- **环境变量**: `AI_SILICON_FLOW_API_KEY` (设置你的 API Key)。
- **模型**: 默认为 `Qwen/Qwen2.5-7B-Instruct`，可根据需要调整。

#### 本地存储配置
由于项目已停用阿里云 OSS，改为本地存储：
```yaml
file:
  storage:
    type: local
    local:
      base-path: /your/actual/upload/path  # 物理存储路径
      url-prefix: https://yourdomain.com/uploads  # 外部访问前缀
```

### 2.3 构建与运行
1. 进入 `food_menu` 目录。
2. 执行构建：`mvn clean package -DskipTests`。
3. 运行 JAR 包：
   ```bash
   java -jar target/food_menu-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
   ```

---

## 3. 管理后台部署 (food_menu_admin)

### 3.1 构建前端
1. 进入 `food_menu_admin` 目录。
2. 安装依赖：`npm install`。
3. 执行构建：`npm run build`。
4. 生成的 `dist` 文件夹即为部署内容。

### 3.2 Nginx 配置参考
需要配置 Nginx 来处理静态资源访问和后端接口转发：
```nginx
server {
    listen 80;
    server_name yourdomain.com;

    # 管理后台静态资源
    location / {
        root /var/www/food_menu_admin/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # 后端接口转发
    location /api/ {
        proxy_pass http://localhost:8080/; # 注意结尾的斜杠
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 本地图片资源转发 (由后端 processImageUrl 拼接)
    location /uploads/ {
        alias /your/actual/upload/path/; # 对应 application.yml 中的 base-path
        autoindex on;
    }
}
```

---

## 4. 小程序部署 (food_menu_uniapp)

### 4.1 环境配置
1. 检查 `food_menu_uniapp/.env.production`。
2. 确保 `VITE_API_URL` 指向生产环境域名（例如 `https://yourdomain.com/api`）。

### 4.2 发布流程
1. 使用 **HBuilderX** 打开项目。
2. 点击菜单栏：**发行** -> **小程序-微信**。
3. 编译完成后，在弹出的小程序开发者工具中点击 **上传**。
4. 在微信公众平台提交审核并发布。

---

## 5. 注意事项
1. **安全**: 生产环境务必更改 `jwt.secret`。
2. **AI 频率限制**: 如果 AI 调用量大，请注意供应商的速率限制 (Rate Limit)。
3. **HTTPS**: 微信小程序强制要求 HTTPS，请确保服务器已配置 SSL 证书。
4. **图片访问**: `processImageUrl()` 会根据 `url-prefix` 拼接完整地址，请确保 Nginx 代理路径与该配置一致。
