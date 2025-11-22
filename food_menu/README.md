# 快速启动指南

## ✅ 数据库已就绪

数据库初始化成功!

- 👥 用户: 2个 (admin, test)
- 📁 分类: 5个
- 🍜 菜品: 6个

## 🚀 启动应用

```bash
# 进入项目目录
cd d:\java\project\foodMenuProject\food_menu

# 启动应用
.\mvnw.cmd spring-boot:run
```

等待启动完成,看到以下日志表示成功:

```
Started FoodMenuApplication in X.XXX seconds
```

## 📚 访问接口文档

浏览器打开: **<http://localhost:8080/doc.html>**

## 🧪 快速测试

### 1. 测试登录 (Knife4j界面)

1. 打开 `用户管理` → `用户登录`
2. 点击 `调试`
3. 输入请求体:

   ```json
   {
     "type": 1,
     "username": "admin",
     "password": "123456"
   }
   ```

4. 点击 `发送` → 复制返回的Token

### 2. 测试查询菜品

1. 打开 `菜品管理` → `查询分类下的菜品`
2. 输入 `categoryId`: 1
3. 点击 `发送` → 查看川菜列表

### 3. 测试提交订单

1. 打开 `订单管理` → `提交订单`
2. 在顶部 `Authorization` 输入: `Bearer {你的Token}`
3. 输入请求体:

   ```json
   {
     "address": "北京市朝阳区xxx",
     "consignee": "张三",
     "phone": "13800138000",
     "orderDetails": [
       {
         "dishId": 1,
         "name": "宫保鸡丁",
         "number": 2,
         "amount": 38.00
       }
     ]
   }
   ```

4. 点击 `发送` → 订单创建成功

## 📋 测试账号

| 用户名 | 密码 | 手机号 |
|--------|------|--------|
| admin | 123456 | 13800138000 |
| test | 123456 | 13800138001 |

**验证码登录**: 验证码固定为 `1234`

## 🎯 主要功能

- ✅ 用户登录 (用户名/密码 或 手机号/验证码)
- ✅ 菜品查询 (按分类、分页)
- ✅ 订单提交 (自动计算金额)
- ✅ 订单查询 (分页、详情)
- ✅ 订单状态管理

## 📞 遇到问题?

1. **端口被占用**: 修改 `application.yml` 中的 `server.port`
2. **数据库连接失败**: 检查MySQL是否启动,密码是否正确
3. **编译失败**: 确认Java版本是否为17+

---

**详细文档**: 查看 [walkthrough.md](file:///C:/Users/long/.gemini/antigravity/brain/7fe713db-ea75-4c6b-b5a6-a32ed495ab56/walkthrough.md)
