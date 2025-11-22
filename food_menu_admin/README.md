# NeoFood Admin (食菜单管理端)

前沿潮流的可视化后台，围绕 `food_menu` Spring Boot 服务打造。技术栈：

- **构建**：Vite 5 + Vue 3 + TypeScript
- **状态&路由**：Pinia、Vue Router
- **UI & 动效**：Naive UI、UnoCSS、定制玻璃拟态/霓虹动画
- **图表**：ECharts
- **工程**：ESLint 9、TypeScript 严格模式、TS 路径别名

## 开发

```bash
cd food_menu_admin
npm install   # 首次安装依赖，需要 Node 18+
npm run dev   # http://localhost:5173
```

> 当前工作机 Node 14 会看到 `Unsupported engine` 的警告，推荐升级到 Node 18+ 以获得完整的 Vite/ESLint 能力。

## 目录速览

```
src/
 ├─ api/            # Axios 封装 & 模块调用
 ├─ components/     # 炫酷仪表盘组件：StatCard、OrderStream、FlavorNetwork...
 ├─ layouts/        # DefaultLayout：玻璃拟态侧边栏 + 头部交互
 ├─ router/         # 权限路由，登录态校验
 ├─ store/          # Pinia 用户/Token 管理
 ├─ styles/         # 主题、动画、SCSS 混入
 └─ views/          # 登录、Dashboard、菜品、订单等页面
```

## 后端联调

- `vite.config.ts` 已配置 `/api` 代理至 `http://localhost:8080`
- 默认登录 admin/123456，登录成功后将 token 写入 `localStorage`
- `src/api/modules.ts` 示例封装了登录、菜品、订单接口，可按需扩展

## 设计语言

- 深色星舰背景、渐变玻璃卡片、霓虹呼吸灯
- 高级交互：悬浮起伏、动态粒子、时间线/流图控件
- 数据驾驶舱 + 菜品矩阵 + 订单心跳，满足可视化驾驶需求

