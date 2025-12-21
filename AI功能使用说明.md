# AI功能集成使用说明

## 📋 功能概述

本次为家庭菜单管理系统集成了AI功能,提供以下核心能力:

1. **AI对话助手** - 与AI进行自然对话,咨询菜品相关问题
2. **智能菜品推荐** - 基于用户偏好和历史数据推荐菜品
3. **智能下单解析** - 解析自然语言下单(如"我要两份宫保鸡丁")
4. **一周菜单生成** - AI自动生成一周的菜单规划

## 🚀 快速开始

### 1. 配置AI服务

#### 获取硅基流动API密钥

1. 访问 [硅基流动官网](https://siliconflow.cn)
2. 注册并登录账号
3. 进入控制台,创建API密钥
4. 复制API密钥

#### 配置application.yml

在 `application.yml` 中配置AI服务:

```yaml
ai:
  provider: silicon_flow  # 当前使用的AI服务商
  silicon-flow:
    api-key: your-api-key-here  # 替换为你的API密钥
    base-url: https://api.siliconflow.cn/v1
    model: Qwen/Qwen2.5-7B-Instruct
    max-tokens: 2000
    temperature: 0.7
    timeout: 30000
```

**推荐方式**: 通过环境变量配置API密钥(更安全)

```bash
# Windows
set AI_SILICON_FLOW_API_KEY=your-api-key-here

# Linux/Mac
export AI_SILICON_FLOW_API_KEY=your-api-key-here
```

### 2. 创建数据库表

执行SQL脚本创建AI对话历史表:

```bash
# 在MySQL中执行
source food_menu/src/main/resources/sql/ai_chat_history.sql
```

或手动执行:

```sql
CREATE TABLE IF NOT EXISTS ai_chat_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    wx_user_id BIGINT NOT NULL COMMENT '微信用户ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    role VARCHAR(20) NOT NULL COMMENT '角色: user/assistant',
    content TEXT NOT NULL COMMENT '消息内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_wx_user_id (wx_user_id),
    INDEX idx_family_id (family_id),
    INDEX idx_create_time (create_time)
) COMMENT 'AI对话历史表';
```

### 3. 启动服务

```bash
cd food_menu
mvn spring-boot:run
```

### 4. 小程序配置

在小程序的 `pages.json` 中添加AI助手页面:

```json
{
  "path": "pages/ai/ai-assistant",
  "style": {
    "navigationBarTitleText": "AI美食助手"
  }
}
```

## 📱 小程序端使用

### AI对话助手

1. 进入"AI美食助手"页面
2. 输入消息或点击快捷按钮
3. AI会根据上下文回复

**示例对话:**

- "今天吃什么?"
- "推荐一些清淡的菜品"
- "有什么适合小孩吃的菜?"

### 智能推荐

调用推荐接口:

```javascript
const res = await request({
  url: '/wx/ai/recommend',
  method: 'POST',
  data: {
    mealPeriod: '午餐',
    preferences: '清淡少油',
    count: 3
  }
})
```

### 智能下单

```javascript
const res = await request({
  url: '/wx/ai/parse-order',
  method: 'POST',
  data: {
    text: '我要两份宫保鸡丁和一份鱼香肉丝'
  }
})
```

## 🔧 管理端使用

### 生成一周菜单

```javascript
const res = await request({
  url: '/admin/ai/generate-weekly-menu',
  method: 'POST',
  data: {
    preferences: '营养均衡、荤素搭配、预算控制在每天50元'
  }
})
```

## 🔄 切换AI服务商

### 切换到通义千问

1. 修改 `application.yml`:

```yaml
ai:
  provider: qwen  # 改为qwen
  qwen:
    api-key: your-qwen-api-key
    base-url: https://dashscope.aliyuncs.com/api/v1
    model: qwen-turbo
```

2. 实现 `QwenAiServiceImpl` 的具体逻辑(参考 `SiliconFlowAiServiceImpl`)

3. 重启服务

### 切换到本地Ollama

1. 安装Ollama并启动服务
2. 修改配置:

```yaml
ai:
  provider: ollama
  ollama:
    base-url: http://localhost:11434
    model: qwen2.5:7b
```

3. 实现 `OllamaAiServiceImpl` 的具体逻辑

## 📊 API接口文档

### 小程序端接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/wx/ai/chat` | POST | AI对话 |
| `/wx/ai/recommend` | POST | 获取AI推荐菜品 |
| `/wx/ai/quick-recommend` | GET | 快速推荐 |
| `/wx/ai/parse-order` | POST | 智能解析下单文本 |
| `/wx/ai/clear-history` | DELETE | 清除对话历史 |

### 管理端接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/admin/ai/generate-weekly-menu` | POST | 生成一周菜单 |
| `/admin/ai/analyze-preferences` | GET | 分析用户偏好 |

## ⚠️ 注意事项

### API密钥安全

1. **不要**将API密钥硬编码在代码中
2. **不要**将API密钥提交到Git仓库
3. **推荐**使用环境变量或Jasypt加密

### 免费额度限制

- 硅基流动有每日免费调用限制
- 建议添加调用次数统计和限流
- 超出限制时优雅降级(返回默认推荐)

### 性能优化建议

1. **缓存常见问题的回答**,减少API调用
2. **限制对话历史长度**,避免token消耗过大
3. **异步处理**,避免阻塞用户请求

## 🐛 常见问题

### Q: AI服务返回"暂时不可用"

**A:** 检查以下几点:

1. API密钥是否正确配置
2. 网络是否正常
3. 免费额度是否用完
4. 查看后端日志获取详细错误信息

### Q: 如何查看AI服务调用日志?

**A:** 查看日志文件:

```bash
tail -f logs/spring.log | grep "AI"
```

### Q: 如何切换AI服务商?

**A:** 修改 `application.yml` 中的 `ai.provider` 配置,重启服务即可。

## 📈 后续优化建议

1. **多模型支持** - 同时配置多个AI服务,根据场景选择
2. **用户反馈** - 添加"有用"/"无用"按钮,优化推荐算法
3. **语音交互** - 集成语音识别,支持语音点餐
4. **图像识别** - 上传菜品图片,AI识别菜名
5. **个性化学习** - 根据用户反馈持续优化推荐

## 📞 技术支持

如有问题,请查看:

- 实施方案: `AI功能集成实施方案.md`
- 代码注释: 所有代码都有详细的中文注释
- 日志文件: `logs/spring.log`
