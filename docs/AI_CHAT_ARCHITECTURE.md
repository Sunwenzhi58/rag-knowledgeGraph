# AI 对话功能 - 架构设计文档

## 系统架构

### 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                          前端层                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │                    AiChat.vue                          │ │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────────────┐    │ │
│  │  │ 输入框    │  │ 消息列表  │  │ EventSource 客户端 │   │ │
│  │  └──────────┘  └──────────┘  └──────────────────┘    │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ HTTP + SSE
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                          后端层                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │              AiChatController (控制层)                  │ │
│  │  GET /ai-chat/stream?message=xxx&sessionId=xxx         │ │
│  │  DELETE /ai-chat/session/{sessionId}                   │ │
│  └─────────────────────┬──────────────────────────────────┘ │
│                        │                                     │
│  ┌─────────────────────▼──────────────────────────────────┐ │
│  │          AiChatServiceImpl (业务层)                     │ │
│  │  ┌──────────────┐  ┌──────────────┐                   │ │
│  │  │ 会话管理      │  │ 流式处理      │                   │ │
│  │  │ ConcurrentHashMap│ SseEmitter  │                   │ │
│  │  └──────────────┘  └──────────────┘                   │ │
│  └─────────────────────┬──────────────────────────────────┘ │
│                        │                                     │
│  ┌─────────────────────▼──────────────────────────────────┐ │
│  │              Spring AI (AI 集成层)                      │ │
│  │  ┌──────────────┐  ┌──────────────┐                   │ │
│  │  │ ChatModel     │  │ Prompt       │                   │ │
│  │  │ (DashScope)   │  │ 构建器        │                   │ │
│  │  └──────────────┘  └──────────────┘                   │ │
│  └─────────────────────┬──────────────────────────────────┘ │
└────────────────────────┼──────────────────────────────────┘
                         │
                         │ API 调用
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                      AI 模型服务                             │
│  ┌────────────────────────────────────────────────────────┐ │
│  │          DashScope API (阿里云通义千问)                │ │
│  │              或 OpenAI API                              │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 核心组件详解

### 1. 前端组件（AiChat.vue）

#### 主要职责
- 用户交互界面
- SSE 连接管理
- 消息展示和格式化
- 会话 ID 管理

#### 核心方法

```javascript
// 1. 发送消息
sendMessage() {
  // 添加用户消息到消息列表
  // 调用 streamChat() 建立 SSE 连接
  // 清空输入框
}

// 2. 流式对话
streamChat(message, aiMessage) {
  // 创建 EventSource 连接
  // 监听 'message' 事件，累积 AI 回复
  // 监听 'done' 事件，关闭连接
  // 监听 'error' 事件，处理错误
}

// 3. 清空对话
clearChat() {
  // 调用后端 DELETE 接口
  // 清空本地消息列表
  // 生成新的 sessionId
}
```

#### 数据结构

```javascript
data() {
  return {
    messages: [
      {
        role: 'user' | 'assistant',
        content: '消息内容',
        time: '14:30'
      }
    ],
    inputMessage: '',        // 当前输入
    isLoading: false,        // 是否正在加载
    sessionId: 'session_xxx' // 会话 ID
  }
}
```

### 2. 后端 Controller（AiChatController）

#### 主要职责
- 接收 HTTP 请求
- 创建 SseEmitter 对象
- 调用 Service 层处理业务
- 管理 SSE 连接生命周期

#### 接口定义

```java
@RestController
@RequestMapping("/ai-chat")
public class AiChatController {
    
    // 流式对话接口
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(
        @RequestParam("message") String message,
        @RequestParam(value = "sessionId", required = false) String sessionId
    )
    
    // 清除会话接口
    @DeleteMapping("/session/{sessionId}")
    public void clearSession(@PathVariable String sessionId)
}
```

### 3. 业务层（AiChatServiceImpl）

#### 主要职责
- 会话管理（存储、检索、清除）
- 构建对话提示（Prompt）
- 调用 Spring AI 进行对话
- 处理流式响应
- 更新会话历史

#### 核心流程

```java
public void streamChat(String message, String sessionId, SseEmitter emitter) {
    // 1. 获取或创建会话历史
    List<Message> history = getOrCreateSession(sessionId);
    
    // 2. 添加用户消息到历史
    history.add(new UserMessage(message));
    
    // 3. 构建 Prompt（包含系统提示 + 历史消息）
    Prompt prompt = buildPrompt(history);
    
    // 4. 调用 ChatModel 进行流式对话
    Flux<String> flux = chatModel.stream(prompt);
    
    // 5. 订阅流，发送 SSE 事件
    flux.subscribe(
        chunk -> emitter.send(event().name("message").data(chunk)),
        error -> emitter.completeWithError(error),
        () -> {
            history.add(new AssistantMessage(fullResponse));
            emitter.send(event().name("done").data("[DONE]"));
            emitter.complete();
        }
    );
}
```

#### 会话管理

```java
// 内存存储（使用 ConcurrentHashMap）
private Map<String, List<Message>> sessionHistory = new ConcurrentHashMap<>();

// 结构示意：
{
  "session_123": [
    SystemMessage("你是一个助手..."),
    UserMessage("你好"),
    AssistantMessage("你好！很高兴见到你..."),
    UserMessage("介绍一下自己"),
    AssistantMessage("我是派聪明知识助手...")
  ]
}
```

### 4. Spring AI 集成

#### ChatModel 配置

```java
@Autowired
@Qualifier("dashScopeChatModel")
private ChatModel chatModel;
```

#### Prompt 构建

```java
List<Message> promptMessages = new ArrayList<>();
promptMessages.add(new SystemMessage(SYSTEM_PROMPT));  // 系统提示
promptMessages.addAll(history);                        // 历史对话
Prompt prompt = new Prompt(promptMessages);
```

#### 流式调用

```java
Flux<String> flux = chatModel.stream(prompt)
    .map(response -> response.getResult().getOutput().getText());
```

## 通信流程

### 流式对话时序图

```
用户      前端          后端Controller    后端Service      Spring AI      DashScope
 │         │                │                │               │              │
 │  输入   │                │                │               │              │
 ├────────>│                │                │               │              │
 │         │  GET /stream   │                │               │              │
 │         ├───────────────>│                │               │              │
 │         │                │ streamChat()   │               │              │
 │         │                ├───────────────>│               │              │
 │         │                │                │ stream()      │              │
 │         │                │                ├──────────────>│              │
 │         │                │                │               │ API Call     │
 │         │                │                │               ├─────────────>│
 │         │                │                │               │              │
 │         │                │                │               │<─ chunk 1 ───┤
 │         │                │                │<── chunk 1 ───┤              │
 │         │                │<─ SSE chunk 1 ─┤               │              │
 │         │<─ EventSource ─┤                │               │              │
 │  显示1  │                │                │               │              │
 │<────────┤                │                │               │              │
 │         │                │                │               │<─ chunk 2 ───┤
 │         │                │                │<── chunk 2 ───┤              │
 │         │                │<─ SSE chunk 2 ─┤               │              │
 │         │<─ EventSource ─┤                │               │              │
 │  显示2  │                │                │               │              │
 │<────────┤                │                │               │              │
 │         │                │                │               │    ...       │
 │         │                │                │               │<─── done ────┤
 │         │                │                │<──── done ────┤              │
 │         │                │<─ SSE done ────┤               │              │
 │         │<─ EventSource ─┤                │               │              │
 │  完成   │                │                │               │              │
 │<────────┤                │                │               │              │
```

## SSE 事件详解

### 事件类型

1. **message 事件**
   - 用途：传输消息片段
   - 数据：文本内容
   - 频率：持续发送直到完成

```javascript
event: message
data: 你好
```

2. **done 事件**
   - 用途：标识对话完成
   - 数据：[DONE] 标记
   - 频率：每次对话结束时发送一次

```javascript
event: done
data: [DONE]
```

3. **error 事件**
   - 用途：传输错误信息
   - 数据：错误描述
   - 频率：发生错误时发送

```javascript
event: error
data: 对话出错：连接超时
```

### SSE 连接管理

```java
// 创建 SseEmitter（60秒超时）
SseEmitter emitter = new SseEmitter(60000L);

// 发送事件
emitter.send(SseEmitter.event()
    .name("message")
    .data("内容"));

// 完成连接
emitter.complete();

// 错误处理
emitter.completeWithError(exception);

// 超时处理
emitter.onTimeout(() -> emitter.complete());
```

## 数据流转

### 用户消息流

```
用户输入 → 前端消息列表 → HTTP 请求 → 后端 Controller → 
Service 层 → 添加到会话历史 → 构建 Prompt → 
Spring AI → DashScope API → AI 模型
```

### AI 响应流

```
AI 模型 → DashScope API → Spring AI → Flux Stream → 
Service 层（订阅） → SSE 发送 → 前端 EventSource → 
累积显示 → 完成后添加到历史 → 前端消息列表
```

## 会话管理机制

### 会话生命周期

```
1. 创建会话
   ├─ 前端生成 sessionId
   └─ 首次请求时后端创建会话存储

2. 会话使用
   ├─ 每次对话传递 sessionId
   ├─ 后端根据 sessionId 获取历史
   └─ 对话完成后更新历史

3. 会话清除
   ├─ 用户点击"清空对话"
   ├─ 前端调用 DELETE 接口
   ├─ 后端删除会话存储
   └─ 前端生成新 sessionId
```

### 会话存储策略

**当前实现**：内存存储（ConcurrentHashMap）
- 优点：速度快、实现简单
- 缺点：服务重启丢失、不支持分布式

**可选方案**：

1. **Redis 存储**
```java
@Autowired
private RedisTemplate<String, List<Message>> redisTemplate;

public List<Message> getSession(String sessionId) {
    return redisTemplate.opsForValue().get(sessionId);
}
```

2. **数据库存储**
```sql
CREATE TABLE chat_session (
    session_id VARCHAR(100) PRIMARY KEY,
    messages TEXT,  -- JSON 格式
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

## 性能考虑

### 并发处理

- SSE 连接数限制
- 线程池配置
- 超时设置

### 内存管理

- 会话历史长度限制
- 定期清理过期会话
- 消息数量限制

### 网络优化

- Gzip 压缩
- 分块传输
- 连接复用

## 扩展点

### 1. 知识库集成

```java
// 在 Prompt 构建时添加知识检索
String knowledge = knowledgeService.retrieve(message);
promptMessages.add(new SystemMessage("参考信息：" + knowledge));
```

### 2. 多模型支持

```java
@Autowired
@Qualifier("openAiChatModel")
private ChatModel openAiModel;

// 根据配置选择模型
ChatModel selectedModel = useOpenAI ? openAiModel : dashScopeModel;
```

### 3. 对话持久化

```java
// 每次对话完成后保存
chatHistoryRepository.save(sessionId, messages);
```

### 4. 流式进度回调

```java
emitter.send(event().name("progress").data("已生成 50%"));
```

## 安全考虑

### 1. 认证授权

```java
// 添加 JWT 验证
String userId = jwtUtils.extractUserId(token);
```

### 2. 频率限制

```java
// 使用 Redis 实现限流
if (rateLimiter.isAllowed(userId)) {
    // 允许请求
}
```

### 3. 内容过滤

```java
// 敏感词过滤
String filteredMessage = contentFilter.filter(message);
```

### 4. 会话隔离

```java
// 确保用户只能访问自己的会话
if (!sessionBelongsToUser(sessionId, userId)) {
    throw new UnauthorizedException();
}
```

## 监控与日志

### 关键指标

- SSE 连接数
- 对话响应时间
- 错误率
- 会话数量

### 日志记录

```java
logger.info("开始对话: sessionId={}, message={}", sessionId, message);
logger.info("对话完成: sessionId={}, duration={}ms", sessionId, duration);
logger.error("对话失败: sessionId={}, error={}", sessionId, error);
```

## 总结

该 AI 对话功能采用了现代化的架构设计：

- **前后端分离**：清晰的职责划分
- **流式响应**：优秀的用户体验
- **会话管理**：上下文连贯的对话
- **可扩展性**：易于集成新功能
- **高性能**：异步处理、事件驱动

通过合理的架构设计和技术选型，实现了一个功能完善、性能优良的 AI 对话系统。

---

**文档版本**: 1.0.0  
**更新日期**: 2026-02-14
