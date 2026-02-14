# AI 对话功能 - 快速启动指南

## 📦 已完成的文件

### 后端文件（Spring Boot）

1. **Controller**
   - `src/main/java/com/sandwich/ragkg/controller/AiChatController.java`
   - 提供 SSE 流式对话接口

2. **Service**
   - `src/main/java/com/sandwich/ragkg/service/AiChatService.java`（接口）
   - `src/main/java/com/sandwich/ragkg/service/impl/AiChatServiceImpl.java`（实现）
   - 处理 AI 对话逻辑和会话管理

### 前端文件（Vue）

3. **View**
   - `vue/src/views/manager/AiChat.vue`
   - 完整的 AI 对话界面

### 文档

4. **使用指南**
   - `docs/AI_CHAT_GUIDE.md`
   - 详细的功能说明和使用文档

## 🚀 快速启动

### 1. 确认配置

检查 `src/main/resources/application.yml` 中的配置：

```yaml
spring:
  ai:
    dashscope:
      api-key: sk-f7184cec969045aba6a20a0b5a1cc193  # 确认 API Key 正确
```

### 2. 启动后端

```bash
# 在项目根目录
cd /Users/sunwenzhi/IdeaProjects/rag-knowledgeGraph

# 使用 Maven 启动
mvn spring-boot:run

# 或使用 IDE 运行 RagKgApplication 主类
```

后端将在 `http://localhost:8081` 启动

### 3. 启动前端

```bash
# 进入前端目录
cd vue

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run serve
```

前端将在 `http://localhost:8080` 启动

### 4. 访问应用

1. 打开浏览器访问 `http://localhost:8080`
2. 使用已有账号登录（或注册新账号）
3. 登录后会自动跳转到 AI 对话页面（`/ai-chat`）

## 🎯 功能特点

### ✨ 核心功能

- **流式响应**: 使用 SSE 技术，实时显示 AI 回复
- **会话管理**: 自动保持对话上下文
- **美观界面**: 现代化的对话界面设计
- **快速提问**: 预设常见问题，一键发送
- **Markdown 支持**: 支持粗体、斜体、代码等格式

### 🎨 界面特性

- 渐变色头像
- 打字动画效果
- 消息淡入动画
- 自动滚动到底部
- 响应式设计（支持移动端）
- 清空对话功能

### ⚙️ 技术特性

- **后端**: Spring AI + SSE + 会话管理
- **前端**: Vue 2 + Element UI + EventSource
- **通信**: Server-Sent Events（单向推送）
- **模型**: DashScope（通义千问）

## 📋 API 接口

### 1. 流式对话

```
GET /ai-chat/stream?message={消息内容}&sessionId={会话ID}
```

**响应**: SSE 流

- 事件 `message`: 消息片段
- 事件 `done`: 对话完成
- 事件 `error`: 错误信息

### 2. 清除会话

```
DELETE /ai-chat/session/{sessionId}
```

## 🔧 配置说明

### 后端配置

**文件**: `application.yml`

```yaml
server:
  port: 8081

spring:
  ai:
    dashscope:
      api-key: your-api-key  # DashScope API Key
```

### 前端配置

**文件**: `vue/.env.development`

```env
VUE_APP_BASEURL=http://localhost:8081
```

## 🎨 自定义配置

### 修改系统提示词

编辑 `AiChatServiceImpl.java` 的 `SYSTEM_PROMPT` 常量：

```java
private static final String SYSTEM_PROMPT = """
    你是一个友好的 AI 助手...
    """;
```

### 修改快速提问

编辑 `AiChat.vue` 的 `quickQuestions` 数组：

```javascript
quickQuestions: [
  '你的问题1',
  '你的问题2',
  '你的问题3'
]
```

### 修改超时时间

在 `AiChatController.java` 中修改：

```java
SseEmitter emitter = new SseEmitter(60000L); // 60秒，可根据需要调整
```

## 🐛 常见问题

### Q1: SSE 连接失败

**可能原因**:
- 后端未启动
- 端口被占用
- CORS 配置问题

**解决方法**:
1. 确认后端在 8081 端口正常运行
2. 检查浏览器控制台错误信息
3. 检查 `SecurityConfig.java` 中的 CORS 配置

### Q2: 没有显示 AI 回复

**检查项**:
1. DashScope API Key 是否正确
2. 网络是否可以访问 DashScope API
3. 查看后端日志是否有错误

### Q3: 对话没有上下文

**原因**: SessionId 没有正确传递

**解决**: 确保前端代码中 `sessionId` 在每次请求时保持不变

## 📝 使用示例

### 基础对话

```javascript
// 发送消息
this.inputMessage = '你好，介绍一下自己'
this.sendMessage()

// 系统会自动处理 SSE 连接和消息显示
```

### 清空对话

```javascript
// 点击"清空对话"按钮
this.clearChat()
```

## 🔄 后续扩展建议

可以在此基础上扩展：

1. **知识库集成**: 接入 RAG 知识检索
2. **文件上传**: 支持上传文档进行分析
3. **对话导出**: 导出为 Markdown 或 PDF
4. **语音对话**: 集成语音识别和合成
5. **多模型切换**: 支持在界面切换不同 AI 模型
6. **对话分享**: 生成分享链接
7. **个性化设置**: 自定义 AI 角色和风格

## 📚 更多文档

详细文档请查看: `docs/AI_CHAT_GUIDE.md`

## ✅ 功能清单

- [x] 后端 SSE Controller
- [x] 对话 Service 层
- [x] 会话管理（内存）
- [x] 前端对话界面
- [x] 流式消息显示
- [x] 快速提问功能
- [x] 清空对话功能
- [x] Markdown 格式支持
- [x] 响应式布局
- [x] 动画效果
- [ ] 知识库集成（待实现）
- [ ] 对话持久化（待实现）
- [ ] 多模型支持（待实现）

## 🎉 开始使用

现在就启动项目，体验智能 AI 对话功能吧！

---

**版本**: 1.0.0  
**创建日期**: 2026-02-14  
**技术栈**: Spring AI + Spring Boot + Vue 2 + Element UI
