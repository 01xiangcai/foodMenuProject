# 家宴菜单 uni-app 版本

## 项目说明

这是将微信小程序原生代码迁移到 uni-app 框架的版本，支持编译为：

- 微信小程序
- H5 网页版
- 其他小程序平台（支付宝、百度等）

## 已完成功能

✅ 项目基础配置
✅ API 请求封装
✅ 首页（轮播图、快捷操作、推荐菜品）
⏳ 菜单页（进行中）
⏳ 详情页（待开发）
⏳ 购物车（待开发）
⏳ 订单功能（待开发）
⏳ 个人中心（待开发）

## 快速开始

### 1. 安装依赖

```bash
cd food_menu_uniapp
npm install
```

### 2. 运行项目

#### 微信小程序

```bash
npm run dev:mp-weixin
```

然后使用微信开发者工具打开 `dist/dev/mp-weixin` 目录。

#### H5 网页版

```bash
npm run dev:h5
```

浏览器访问：`http://localhost:8080`

### 3. 构建生产版本

#### 构建微信小程序

```bash
npm run build:mp-weixin
```

#### 构建 H5

```bash
npm run build:h5
```

## 项目结构

```
food_menu_uniapp/
├── pages/                  # 页面
│   ├── index/             # 首页 ✅
│   ├── menu/              # 菜单页 ⏳
│   ├── detail/            # 详情页 ⏳
│   ├── cart/              # 购物车 ⏳
│   ├── order/             # 订单 ⏳
│   └── profile/           # 个人中心 ⏳
├── components/            # 组件
├── static/                # 静态资源
├── store/                 # 状态管理
├── utils/                 # 工具函数
│   └── request.js        # API 请求封装 ✅
├── api/                   # API 接口
│   └── index.js          # 接口定义 ✅
├── styles/                # 全局样式
│   └── global.scss       # 全局样式 ✅
├── App.vue               # 应用入口 ✅
├── main.js               # 主入口 ✅
├── manifest.json         # 应用配置 ✅
├── pages.json            # 页面配置 ✅
└── uni.scss              # 样式变量 ✅
```

## 配置说明

### API 地址配置

在 `utils/request.js` 中配置：

```javascript
// H5 环境
const BASE_URL = '/api'  // 通过 Nginx 代理

// 微信小程序环境
const BASE_URL = 'http://139.196.146.244/api'  // 直接访问
```

### 微信小程序 AppID

在 `manifest.json` 中配置：

```json
{
  "mp-weixin": {
    "appid": "your-appid-here"
  }
}
```

## 部署说明

### H5 部署

1. 构建生产版本：

   ```bash
   npm run build:h5
   ```

2. 将 `dist/build/h5` 目录上传到服务器：

   ```bash
   scp -r dist/build/h5/* root@139.196.146.244:/www/wwwroot/food-menu-h5/
   ```

3. Nginx 配置已包含 `/h5/` 路径代理

4. 访问：`http://139.196.146.244/h5/`

### 微信小程序部署

1. 构建生产版本：

   ```bash
   npm run build:mp-weixin
   ```

2. 使用微信开发者工具打开 `dist/build/mp-weixin`

3. 点击"上传"，提交审核

## 下一步开发

1. 完成菜单页（菜品列表、分类筛选）
2. 完成详情页（菜品详情、加入购物车）
3. 完成购物车功能
4. 完成订单流程
5. 完成个人中心
6. 优化样式和交互
7. 测试和bug修复

## 注意事项

- 本项目使用 Vue 3 Composition API
- 不使用 TypeScript
- 使用 Pinia 进行状态管理
- 支持条件编译（#ifdef）区分不同平台
