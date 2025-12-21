# AI功能集成实施方案

## 项目背景

为家庭菜单管理系统集成AI功能,提供智能菜品推荐、AI对话助手和智能下单等功能。采用**策略模式**设计,确保后期可以灵活切换不同的AI服务提供商(硅基流动、通义千问、本地部署等),无需大规模修改代码。

## 用户需要审核的内容

> [!IMPORTANT]
> **架构设计关键决策**
>
> 1. **策略模式设计**: 使用策略模式实现AI服务抽象层,支持运行时动态切换AI服务提供商
> 2. **配置驱动**: 通过配置文件控制使用哪个AI服务商,切换时只需修改配置,无需改代码
> 3. **初始实现**: 首先实现硅基流动(免费),同时预留通义千问、本地Ollama等扩展接口
> 4. **数据库设计**: 新增AI对话历史表,记录用户与AI的交互,用于优化推荐

> [!WARNING]
> **需要添加的依赖**
>
> - OkHttp3: 用于HTTP请求调用AI API
> - Jackson: JSON序列化(已有)
>
> 这些依赖都是轻量级的,不会影响现有系统性能

## 提议的变更

### 核心架构层

#### 1. AI服务抽象层

##### [NEW] [AiProvider.java](file:///d:/java/project/foodMenuProject/food_menu/src/main/java/com/yao/food_menu/enums/AiProvider.java)

- AI服务提供商枚举: `SILICON_FLOW`(硅基流动), `QWEN`(通义千问), `OLLAMA`(本地部署)

##### [NEW] [AiService.java](file:///d:/java/project/foodMenuProject/food_menu/src/main/java/com/yao/food_menu/service/ai/AiService.java)

- AI服务核心接口,定义统一的AI能力
- `chat()`: 对话功能
- `recommendDishes()`: 菜品推荐
- `parseOrderText()`: 智能解析下单文本
- `generateWeeklyMenu()`: 生成一周菜单

##### [NEW] [AiServiceFactory.java](file:///d:/java/project/foodMenuProject/food_menu/src/main/java/com/yao/food_menu/service/ai/AiServiceFactory.java)

- AI服务工厂类,根据配置返回对应的AI服务实现
- 支持运行时动态切换服务提供商

---

#### 2. AI服务实现层

##### [NEW] [SiliconFlowAiServiceImpl.java](file:///d:/java/project/foodMenuProject/food_menu/src/main/java/com/yao/food_menu/service/ai/impl/SiliconFlowAiServiceImpl.java)

- 硅基流动API实现
- 兼容OpenAI格式的API调用
- 支持流式和非流式响应

##### [NEW] [QwenAiServiceImpl.java](file:///d:/java/project/foodMenuProject/food_menu/src/main/java/com/yao/food_menu/service/ai/impl/QwenAiServiceImpl.java)

- 通义千问API实现(预留,暂不实现具体逻辑)
- 为后期切换做准备

##### [NEW] [OllamaAiServiceImpl.java](file:///d:/java/project/foodMenuProject/food_menu/src/main/java/com/yao/food_menu/service/ai/impl/OllamaAiServiceImpl.java)

- 本地Ollama实现(预留,暂不实现具体逻辑)
- 为后期本地部署做准备

---

#### 3. AI业务服务层

##### [NEW] [MenuRecommendService.java](file:///d:/java/project/foodMenuProject/food_menu/src/main/java/com/yao/food_menu/service/MenuRecommendService.java)

- 菜品推荐业务服务
- 基于用户历史订单、收藏、评论等数据生成个性化推荐
- 调用AI服务生成推荐结果

##### [NEW] [SmartOrderService.java](file:///d:/java/project/foodMenuProject/food_menu/src/main/java/com/yao/food_menu/service/SmartOrderService.java)

- 智能下单服务
- 解析用户输入的自然语言(如"我要两份宫保鸡丁")
- 返回识别的菜品和数量

##### [NEW] [AiChatService.java](file:///d:/java/project/foodMenuProject/food_menu/src/main/java/com/yao/food_menu/service/AiChatService.java)

- AI对话服务
- 管理对话历史
- 提供上下文感知的对话能力

---

### 数据层

#### 数据库表设计

##### [NEW] [ai_chat_history.sql](file:///d:/java/project/foodMenuProject/food_menu/src/main/resources/sql/ai_chat_history.sql)

```sql
CREATE TABLE ai_chat_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    wx_user_id BIGINT COMMENT '微信用户ID',
    family_id BIGINT COMMENT '家庭ID',
    role VARCHAR(20) COMMENT '角色: user/assistant',
    content TEXT COMMENT '消息内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_wx_user_id (wx_user_id),
    INDEX idx_family_id (family_id)
) COMMENT 'AI对话历史表';
```

##### [NEW] [AiChatHistory.java](file:///d:/java/project/foodMenuProject/food_menu/src/main/java/com/yao/food_menu/entity/AiChatHistory.java)

- AI对话历史实体类

##### [NEW] [AiChatHistoryMapper.java](file:///d:/java/project/foodMenuProject/food_menu/src/main/java/com/yao/food_menu/mapper/AiChatHistoryMapper.java)

- AI对话历史Mapper

---

### 配置层

#### [MODIFY] [pom.xml](file:///d:/java/project/foodMenuProject/food_menu/pom.xml)

添加OkHttp依赖:

```xml
<!-- OkHttp for AI API calls -->
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.12.0</version>
</dependency>
```

#### [MODIFY] [application.yml](file:///d:/java/project/foodMenuProject/food_menu/src/main/resources/application.yml)

添加AI配置:

```yaml
# AI服务配置
ai:
  # 当前使用的AI服务提供商: silicon_flow, qwen, ollama
  provider: silicon_flow
  
  # 硅基流动配置
  silicon-flow:
    api-key: ${AI_SILICON_FLOW_API_KEY:your-api-key-here}
    base-url: https://api.siliconflow.cn/v1
    model: Qwen/Qwen2.5-7B-Instruct
    max-tokens: 2000
    temperature: 0.7
    timeout: 30000
  
  # 通义千问配置(预留)
  qwen:
    api-key: ${AI_QWEN_API_KEY:}
    base-url: https://dashscope.aliyuncs.com/api/v1
    model: qwen-turbo
  
  # Ollama配置(预留)
  ollama:
    base-url: http://localhost:11434
    model: qwen2.5:7b
```

#### [NEW] [AiConfig.java](file:///d:/java/project/foodMenuProject/food_menu/src/main/java/com/yao/food_menu/config/AiConfig.java)

- AI配置类,读取配置文件
- 使用`@ConfigurationProperties`绑定配置

---

### 控制器层

#### 小程序端接口

##### [NEW] [WxAiAssistantController.java](file:///d:/java/project/foodMenuProject/food_menu/src/main/java/com/yao/food_menu/controller/WxAiAssistantController.java)

- `POST /wx/ai/chat`: AI对话接口
- `GET /wx/ai/recommend`: 获取AI推荐菜品
- `POST /wx/ai/parse-order`: 智能解析下单文本
- `DELETE /wx/ai/clear-history`: 清除对话历史

#### 管理端接口

##### [NEW] [AdminAiMenuController.java](file:///d:/java/project/foodMenuProject/food_menu/src/main/java/com/yao/food_menu/controller/AdminAiMenuController.java)

- `POST /admin/ai/generate-weekly-menu`: 生成一周菜单
- `GET /admin/ai/analyze-preferences`: 分析用户偏好

---

### DTO层

##### [NEW] [AiChatRequest.java](file:///d:/java/project/foodMenuProject/food_menu/src/main/java/com/yao/food_menu/dto/AiChatRequest.java)

- AI对话请求DTO
- 字段: `message`, `includeHistory`

##### [NEW] [AiChatResponse.java](file:///d:/java/project/foodMenuProject/food_menu/src/main/java/com/yao/food_menu/dto/AiChatResponse.java)

- AI对话响应DTO
- 字段: `reply`, `suggestions`(推荐的菜品ID列表)

##### [NEW] [DishRecommendRequest.java](file:///d:/java/project/foodMenuProject/food_menu/src/main/java/com/yao/food_menu/dto/DishRecommendRequest.java)

- 菜品推荐请求DTO
- 字段: `mealPeriod`(餐次), `preferences`(偏好), `count`(推荐数量)

##### [NEW] [SmartOrderRequest.java](file:///d:/java/project/foodMenuProject/food_menu/src/main/java/com/yao/food_menu/dto/SmartOrderRequest.java)

- 智能下单请求DTO
- 字段: `text`(用户输入的文本)

##### [NEW] [SmartOrderResponse.java](file:///d:/java/project/foodMenuProject/food_menu/src/main/java/com/yao/food_menu/dto/SmartOrderResponse.java)

- 智能下单响应DTO
- 字段: `items`(识别的菜品和数量列表)

---

### 前端开发

#### 小程序端

##### [NEW] [ai-assistant.vue](file:///d:/java/project/foodMenuProject/food_menu_uniapp/src/pages/ai/ai-assistant.vue)

- AI助手主页面
- 炫酷的对话界面(气泡动画、打字效果)
- 支持深浅主题切换
- 快捷推荐按钮(今天吃什么、清淡菜品等)

##### [NEW] [smart-recommend.vue](file:///d:/java/project/foodMenuProject/food_menu_uniapp/src/components/ai/smart-recommend.vue)

- 智能推荐组件
- 可嵌入到首页、菜品列表等页面
- 卡片式展示推荐菜品

#### 管理端

##### [NEW] [AiMenuGenerator.vue](file:///d:/java/project/foodMenuProject/food_menu_admin/src/views/ai/AiMenuGenerator.vue)

- AI菜单生成工具
- 一键生成一周菜单
- 可调整参数(预算、营养偏好等)
- 支持导出和发布

---

## 验证计划

### 自动化测试

#### 1. 单元测试

```bash
# 在food_menu目录下运行
mvn test -Dtest=AiServiceTest
```

测试内容:

- AI服务工厂正确返回对应的实现
- 硅基流动API调用成功
- 配置切换功能正常

#### 2. 集成测试

创建测试类 `AiIntegrationTest.java`:

- 测试完整的对话流程
- 测试菜品推荐功能
- 测试智能下单解析

### 手动验证

#### 1. 小程序端验证

1. 启动后端服务: `mvn spring-boot:run`
2. 启动小程序: 在HBuilderX中运行到微信开发者工具
3. 测试步骤:
   - 进入AI助手页面
   - 发送消息"今天吃什么",查看AI回复
   - 点击推荐的菜品,验证跳转正常
   - 测试智能下单:"我要两份宫保鸡丁"
   - 切换深浅主题,验证UI适配

#### 2. 管理端验证

1. 登录管理后台
2. 进入AI菜单生成页面
3. 点击"生成一周菜单"
4. 查看生成结果是否合理
5. 测试导出功能

#### 3. API切换验证

1. 修改`application.yml`中的`ai.provider`为`qwen`(需要先实现)
2. 重启服务
3. 验证功能是否正常(证明切换机制有效)

### 性能验证

1. 使用JMeter测试AI接口并发性能
2. 验证响应时间在3秒内
3. 检查是否有内存泄漏

---

## 实施顺序

1. ✅ **阶段一**: 基础架构(1-2小时)
   - 添加依赖
   - 创建抽象层和配置

2. ✅ **阶段二**: 硅基流动集成(2-3小时)
   - 实现API调用
   - 测试基本对话功能

3. ✅ **阶段三**: 业务服务(3-4小时)
   - 实现推荐、下单等业务逻辑
   - 创建数据库表

4. ✅ **阶段四**: 接口开发(2小时)
   - 小程序端接口
   - 管理端接口

5. ✅ **阶段五**: 前端开发(4-5小时)
   - 小程序AI助手页面
   - 管理端菜单生成工具

6. ✅ **阶段六**: 测试优化(2小时)
   - 功能测试
   - 性能优化

**预计总时间: 14-17小时**

---

## 后期扩展建议

1. **多模型支持**: 可以同时配置多个AI服务,根据场景选择(如推荐用Qwen,对话用ChatGLM)
2. **缓存优化**: 对常见问题的回答进行缓存,减少API调用
3. **用户反馈**: 添加"回答有用"/"无用"按钮,优化推荐算法
4. **语音交互**: 集成语音识别,支持语音点餐
5. **图像识别**: 上传菜品图片,AI识别菜名

---

## 风险提示

> [!CAUTION]
> **API密钥安全**
>
> - 硅基流动API密钥不要硬编码在代码中
> - 建议通过环境变量`AI_SILICON_FLOW_API_KEY`传入
> - 生产环境使用Jasypt加密

> [!WARNING]
> **免费额度限制**
>
> - 硅基流动有每日免费调用限制
> - 建议添加调用次数统计和限流
> - 超出限制时优雅降级(返回默认推荐)
