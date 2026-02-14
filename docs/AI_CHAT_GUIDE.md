# AI 对话功能使用指南

## 功能概述

基于 Spring AI 和 SSE（Server-Sent Events）实现的实时 AI 对话功能，支持流式响应和会话历史管理。

## 技术架构

### 后端技术栈
- **Spring AI**: AI 模型集成（支持 DashScope 和 OpenAI）
- **SSE (Server-Sent Events)**: 服务器推送技术，实现流式响应
- **Spring Boot 3.x**: 后端框架
- **会话管理**: 基于内存的会话历史存储

### 前端技术栈
- **Vue 2.6**: 前端框架
- **Element UI**: UI 组件库
- **EventSource API**: SSE 客户端
- **响应式设计**: 支持移动端和桌面端

## 项目结构

```
后端：
├── controller/
│   └── AiChatController.java        # SSE 对话控制器
├── service/
│   ├── AiChatService.java           # 对话服务接口
│   └── impl/
│       └── AiChatServiceImpl.java   # 对话服务实现

前端：
└── views/manager/
    └── AiChat.vue                   # AI 对话界面
```

## 核心功能

### 1. 流式对话
- 使用 SSE 实现实时流式响应
- 逐字显示 AI 回复内容
- 优化用户体验

### 2. 会话管理
- 自动生成唯一会话 ID
- 保持对话上下文
- 支持清空会话历史

### 3. 友好界面
- 现代化对话界面设计
- 支持快速提问
- Markdown 格式支持
- 打字动画效果
- 响应式布局

## API 接口

### 1. 流式对话接口

**请求方式**: `GET`  
**接口路径**: `/ai-chat/stream`  
**请求参数**:
- `message` (必填): 用户消息内容
- `sessionId` (可选): 会话 ID，用于保持上下文

**响应格式**: SSE（Server-Sent Events）

**事件类型**:
- `message`: 消息片段
- `done`: 对话完成
- `error`: 错误信息

**示例**:
```javascript
const eventSource = new EventSource(
  'http://localhost:8081/ai-chat/stream?message=你好&sessionId=session_123'
)

eventSource.addEventListener('message', (event) => {
  console.log('收到消息:', event.data)
})

eventSource.addEventListener('done', () => {
  console.log('对话完成')
  eventSource.close()
})
```

### 2. 清除会话接口

**请求方式**: `DELETE`  
**接口路径**: `/ai-chat/session/{sessionId}`  
**路径参数**:
- `sessionId`: 要清除的会话 ID

**示例**:
```javascript
fetch('http://localhost:8081/ai-chat/session/session_123', {
  method: 'DELETE'
})
```

## 使用说明

### 启动项目

1. **启动后端**:
```bash
cd /path/to/rag-knowledgeGraph
mvn spring-boot:run
```

2. **启动前端**:
```bash
cd vue
npm install
npm run serve
```

3. **访问应用**:
打开浏览器访问 `http://localhost:8080`，登录后进入"AI 对话"页面。

### 配置说明

#### 后端配置（application.yml）

```yaml
spring:
  ai:
    dashscope:
      api-key: your-dashscope-api-key  # DashScope API Key
    openai:
      api-key: your-openai-api-key      # OpenAI API Key（可选）

server:
  port: 8081
```

#### 前端配置（.env.development）

```env
VUE_APP_BASEURL=http://localhost:8081
```

## 使用示例

### 前端代码示例

```vue
<template>
  <div>
    <input v-model="message" @keyup.enter="sendMessage" />
    <button @click="sendMessage">发送</button>
    <div v-for="msg in messages" :key="msg.id">
      {{ msg.content }}
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      message: '',
      messages: [],
      sessionId: 'session_' + Date.now()
    }
  },
  methods: {
    sendMessage() {
      const baseURL = 'http://localhost:8081'
      const url = `${baseURL}/ai-chat/stream?message=${encodeURIComponent(this.message)}&sessionId=${this.sessionId}`
      
      const eventSource = new EventSource(url)
      
      const aiMessage = { content: '' }
      this.messages.push(aiMessage)
      
      eventSource.addEventListener('message', (event) => {
        aiMessage.content += event.data
      })
      
      eventSource.addEventListener('done', () => {
        eventSource.close()
      })
      
      this.message = ''
    }
  }
}
</script>
```

## 功能特性

### 1. 智能对话
- 基于 Spring AI 的强大语言模型
- 支持上下文理解
- 准确的问答能力

### 2. 流式响应
- 实时显示 AI 回复
- 降低等待时间
- 提升用户体验

### 3. 会话管理
- 自动维护对话历史
- 支持多轮对话
- 可清空会话重新开始

### 4. 美观界面
- 现代化设计风格
- 流畅的动画效果
- 移动端适配

## 常见问题

### Q1: SSE 连接失败怎么办？
**A**: 检查以下几点：
1. 后端服务是否正常启动
2. 端口 8081 是否被占用
3. CORS 配置是否正确
4. 防火墙是否阻止连接

### Q2: 对话没有上下文怎么办？
**A**: 确保每次请求都传递相同的 `sessionId`，这样服务器才能关联历史对话。

### Q3: 如何修改系统提示词？
**A**: 修改 `AiChatServiceImpl.java` 中的 `SYSTEM_PROMPT` 常量：

```java
private static final String SYSTEM_PROMPT = """
    你的自定义提示词...
    """;
```

### Q4: 如何切换 AI 模型？
**A**: 在 `application.yml` 中配置不同的 API Key，或在代码中修改 `@Qualifier` 注解：

```java
// 使用 DashScope
@Qualifier("dashScopeChatModel")

// 或使用 OpenAI
@Qualifier("openAiChatModel")
```

### Q5: 会话历史存储在哪里？
**A**: 当前使用内存存储（`ConcurrentHashMap`），服务重启后会丢失。如需持久化，可以：
1. 使用 Redis 存储
2. 使用数据库存储
3. 使用文件系统存储

## 性能优化建议

1. **会话清理**: 定期清理过期会话，避免内存泄漏
2. **消息限制**: 限制单次对话的历史消息数量
3. **超时设置**: 合理设置 SSE 超时时间
4. **并发控制**: 限制同时进行的对话数量
5. **缓存优化**: 对常见问题进行缓存

## 扩展功能

可以在此基础上扩展以下功能：

1. **知识库集成**: 结合 RAG 技术，接入知识库
2. **多模态支持**: 支持图片、文件上传
3. **语音对话**: 集成语音识别和合成
4. **对话导出**: 支持导出对话记录
5. **个性化设置**: 支持自定义 AI 角色和风格
6. **协同对话**: 支持多人共享对话会话

## 技术支持

如有问题，请联系开发团队或提交 Issue。

---

**版本**: 1.0.0  
**更新日期**: 2026-02-14
