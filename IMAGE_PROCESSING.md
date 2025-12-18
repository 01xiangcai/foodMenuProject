# 图片处理技术文档

本文档描述后端图片存储和 URL 处理机制。

## 1. 存储模式

系统支持两种存储模式，通过 `file.storage.type` 配置：

| 模式 | 配置值 | 存储位置 | URL 生成方式 |
|------|--------|----------|-------------|
| 本地存储 | `local` | 服务器磁盘 | `urlPrefix` + 相对路径 |
| 阿里云 OSS | `oss` | 云存储 | 预签名 URL (有有效期) |

## 2. 关键配置

### 本地存储 (`file.storage.local.*`)

```yaml
file:
  storage:
    type: local
    local:
      base-path: E:/uploads/food-menu  # 磁盘存储目录
      url-prefix: http://localhost:8080/uploads  # 访问 URL 前缀
      folder: food-menu  # 菜品图片子目录
      avatar-folder: avatars  # 头像子目录
```

### OSS 存储 (`aliyun.oss.*`)

```yaml
aliyun:
  oss:
    endpoint: oss-cn-xxx.aliyuncs.com
    access-key-id: xxx
    access-key-secret: xxx
    bucket-name: xxx
    folder: food-menu
    avatar-folder: avatars
    presigned-url-expiration-hours: 24  # 预签名 URL 有效期
```

## 3. 数据库字段说明

| 表 | 字段 | 本地模式 | OSS 模式 |
|----|------|---------|---------|
| `dish` | `local_image` | 相对路径 | 不使用 |
| `dish` | `local_images` | JSON 相对路径数组 | 不使用 |
| `dish` | `image` | 不使用 | OSS object key |
| `wx_user` | `local_avatar` | 相对路径 | 不使用 |
| `wx_user` | `avatar` | 相对路径 | OSS object key |
| `order_item` | `dish_image` | 混合 (相对路径/完整 URL) | 混合 |

## 4. 后端处理流程

### 4.1 上传

1. 前端调用 `/common/upload` 上传图片
2. `FileStorageFactory` 根据配置选择策略 (`LocalStorageStrategy` / `OssStorageStrategy`)
3. 返回 `objectKey` (相对路径或 OSS key) 和预览 URL

### 4.2 读取

每个 Controller 返回数据前需调用 `processImageUrl()` 方法：

```java
private String processImageUrl(String image, String defaultImage) {
    if (!StringUtils.hasText(image)) return defaultImage;
    if (image.startsWith("http://") || image.startsWith("https://")) return image;
    
    if (fileStorageProperties.isLocal()) {
        // 本地: urlPrefix + path
        return localStorageProperties.getUrlPrefix() + "/" + image;
    } else {
        // OSS: 生成预签名 URL
        return ossService.generatePresignedUrl(image);
    }
}
```

### 4.3 已实现的 Controller

| Controller | 图片处理方法 | 处理对象 |
|------------|-------------|---------|
| `DishController` | `convertImageToPresignedUrl` | 菜品图片 |
| `OrdersController` | `enrichOrderItemImages` + `processImageUrl` | 订单项图片、用户头像 |
| `AdminDailyMealOrderController` | `processImageUrl` | 成员头像、菜品图片 |
| `BannerController` | `convertBannerImageToPresignedUrl` | 轮播图 |
| `WxUserController` | 内联处理 | 用户头像 |

## 5. 前端处理

### Admin (Vue)

通过 Vite 代理：

```typescript
// vite.config.ts
proxy: { '/api': { target: 'http://localhost:8080', rewrite: path => path.replace(/^\/api/, '') } }
```

后端返回完整 URL，前端直接使用。

### Uniapp (小程序)

通过 `request.js` 配置 `baseURL`，后端返回完整 URL。

## 6. 常见问题

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| 图片 404 | 后端返回相对路径 | Controller 添加 `processImageUrl` 处理 |
| 图片过期 | OSS 预签名 URL 超时 | 增大 `presignedUrlExpirationHours` |
| 切换存储模式后图片丢失 | 字段不兼容 | 使用 `ImageMigrationService` 迁移 |
