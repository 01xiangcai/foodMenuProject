# Food Menu Project (家宴点餐系统)

这是一个全栈餐饮点餐系统，包含后端服务、微信小程序用户端和 Web 管理后台。

## 📂 项目结构

- **food_menu**: 后端 API 服务
- **food_menu_weixin**: 微信小程序（用户端）
- **food_menu_admin**: 管理后台（Web 端）

## 🛠 技术栈概览

### 1. 后端服务 (food_menu)

基于 Spring Boot 3 构建的 RESTful API 服务。

- **核心框架**: Spring Boot 3.3.5
- **开发语言**: Java 17
- **数据库/ORM**: MySQL, MyBatis-Plus 3.5.7
- **接口文档**: Knife4j 4.5.0 (OpenAPI 3)
- **安全认证**: JWT (jjwt 0.11.5) + Spring Security Crypto
- **文件存储**: 阿里云 OSS
- **工具库**: Lombok

### 2. 微信小程序 (food_menu_weixin)

原生微信小程序开发，提供用户点餐功能。

- **主要功能**:
  - 首页推荐
  - 菜单浏览与分类
  - 商品详情
  - 购物车与下单（确认订单）
  - 订单列表与详情
  - 收藏夹
  - 用户登录与个人中心
- **UI 风格**: 深色主题 (背景色 `#050a1f`)，定制化 TabBar。

### 3. 管理后台 (food_menu_admin)

基于 Vue 3 的现代化单页应用，用于餐厅管理。

- **核心框架**: Vue 3.5 + Vite 5.4
- **开发语言**: TypeScript
- **UI 组件库**: Naive UI
- **状态管理**: Pinia
- **样式工具**: UnoCSS, Sass
- **数据可视化**: ECharts
- **网络请求**: Axios
