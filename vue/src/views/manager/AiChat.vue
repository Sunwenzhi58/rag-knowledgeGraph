<template>
  <div class="ai-chat-container">
    <!-- 头部 -->
    <div class="chat-header">
      <div class="header-left">
        <i class="el-icon-chat-dot-square"></i>
        <span class="header-title">AI 智能对话</span>
        <el-tag size="mini" type="success" v-if="isConnected">在线</el-tag>
        <el-tag size="mini" type="info" v-else>离线</el-tag>
      </div>
      <div class="header-right">
        <el-button size="small" @click="clearChat" icon="el-icon-delete">清空对话</el-button>
      </div>
    </div>

    <!-- 消息列表 -->
    <div class="chat-messages" ref="messageContainer">
      <div v-if="messages.length === 0" class="empty-state">
        <i class="el-icon-chat-dot-round"></i>
        <p>开始与 AI 助手对话吧！</p>
        <div class="quick-questions">
          <el-tag 
            v-for="(q, index) in quickQuestions" 
            :key="index"
            @click="sendQuickQuestion(q)"
            class="quick-tag">
            {{ q }}
          </el-tag>
        </div>
      </div>

      <div 
        v-for="(msg, index) in messages" 
        :key="index" 
        :class="['message-item', msg.role]">
        <div class="message-avatar">
          <i :class="msg.role === 'user' ? 'el-icon-user' : 'el-icon-cpu'"></i>
        </div>
        <div class="message-content">
          <div class="message-header">
            <span class="message-role">{{ msg.role === 'user' ? '你' : 'AI 助手' }}</span>
            <span class="message-time">{{ msg.time }}</span>
          </div>
          <div class="message-text" v-html="formatMessage(msg.content)"></div>
        </div>
      </div>

      <!-- 加载中提示 -->
      <div v-if="isLoading" class="message-item assistant">
        <div class="message-avatar">
          <i class="el-icon-cpu"></i>
        </div>
        <div class="message-content">
          <div class="message-header">
            <span class="message-role">AI 助手</span>
          </div>
          <div class="message-text typing-indicator">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input-area">
      <el-input
        v-model="inputMessage"
        type="textarea"
        :rows="3"
        placeholder="输入你的问题..."
        @keydown.enter.exact.prevent="sendMessage"
        @keydown.enter.shift.exact="handleShiftEnter"
        :disabled="isLoading">
      </el-input>
      <div class="input-actions">
        <div class="input-hint">
          <span>按 Enter 发送，Shift + Enter 换行</span>
        </div>
        <el-button 
          type="primary" 
          @click="sendMessage"
          :loading="isLoading"
          :disabled="!inputMessage.trim()">
          <i class="el-icon-s-promotion"></i> 发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AiChat',
  data() {
    return {
      messages: [],
      inputMessage: '',
      isLoading: false,
      isConnected: true,
      sessionId: this.generateSessionId(),
      eventSource: null,
      quickQuestions: [
        '你好，请介绍一下自己',
        '如何使用知识库功能？',
        '什么是 RAG 技术？',
        '帮我写一段代码'
      ]
    }
  },
  methods: {
    // 生成会话 ID
    generateSessionId() {
      return 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
    },

    // 发送消息
    async sendMessage() {
      if (!this.inputMessage.trim() || this.isLoading) {
        return
      }

      const userMessage = {
        role: 'user',
        content: this.inputMessage.trim(),
        time: this.formatTime(new Date())
      }

      this.messages.push(userMessage)
      const question = this.inputMessage
      this.inputMessage = ''
      this.isLoading = true

      // 滚动到底部
      this.$nextTick(() => {
        this.scrollToBottom()
      })

      // 创建 AI 消息对象
      const aiMessage = {
        role: 'assistant',
        content: '',
        time: this.formatTime(new Date())
      }
      this.messages.push(aiMessage)

      try {
        await this.streamChat(question, aiMessage)
      } catch (error) {
        console.error('发送消息失败:', error)
        aiMessage.content = '抱歉，发送消息时出现错误，请稍后重试。'
        this.isLoading = false
      }
    },

    // 流式对话
    streamChat(message, aiMessage) {
      return new Promise((resolve, reject) => {
        const baseURL = process.env.VUE_APP_BASEURL || 'http://localhost:8081'
        const url = `${baseURL}/ai-chat/stream?message=${encodeURIComponent(message)}&sessionId=${this.sessionId}`
        
        this.eventSource = new EventSource(url)

        this.eventSource.addEventListener('message', (event) => {
          const chunk = event.data
          aiMessage.content += chunk
          this.$nextTick(() => {
            this.scrollToBottom()
          })
        })

        this.eventSource.addEventListener('done', () => {
          this.eventSource.close()
          this.eventSource = null
          this.isLoading = false
          resolve()
        })

        this.eventSource.addEventListener('error', (event) => {
          console.error('SSE 错误:', event)
          if (event.data) {
            aiMessage.content = event.data
          } else {
            aiMessage.content = '连接中断，请重试。'
          }
          this.eventSource.close()
          this.eventSource = null
          this.isLoading = false
          reject(event)
        })

        this.eventSource.onerror = (error) => {
          console.error('EventSource 连接错误:', error)
          if (!aiMessage.content) {
            aiMessage.content = '连接失败，请检查网络或稍后重试。'
          }
          this.eventSource.close()
          this.eventSource = null
          this.isLoading = false
          reject(error)
        }
      })
    },

    // 快速提问
    sendQuickQuestion(question) {
      this.inputMessage = question
      this.sendMessage()
    },

    // 清空对话
    async clearChat() {
      try {
        await this.$confirm('确定要清空所有对话记录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        // 调用后端清除会话
        const baseURL = process.env.VUE_APP_BASEURL || 'http://localhost:8081'
        await fetch(`${baseURL}/ai-chat/session/${this.sessionId}`, {
          method: 'DELETE'
        })

        this.messages = []
        this.sessionId = this.generateSessionId()
        this.$message.success('对话已清空')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('清空对话失败:', error)
        }
      }
    },

    // 处理 Shift+Enter 换行
    handleShiftEnter(event) {
      // 允许默认的换行行为
    },

    // 格式化消息（支持 Markdown 风格）
    formatMessage(content) {
      if (!content) return ''
      
      // 简单的 Markdown 转换
      let formatted = content
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>') // 粗体
        .replace(/\*(.*?)\*/g, '<em>$1</em>') // 斜体
        .replace(/`(.*?)`/g, '<code>$1</code>') // 行内代码
        .replace(/\n/g, '<br>') // 换行
      
      return formatted
    },

    // 格式化时间
    formatTime(date) {
      const hours = date.getHours().toString().padStart(2, '0')
      const minutes = date.getMinutes().toString().padStart(2, '0')
      return `${hours}:${minutes}`
    },

    // 滚动到底部
    scrollToBottom() {
      const container = this.$refs.messageContainer
      if (container) {
        container.scrollTop = container.scrollHeight
      }
    }
  },

  beforeDestroy() {
    // 关闭 EventSource 连接
    if (this.eventSource) {
      this.eventSource.close()
    }
  }
}
</script>

<style scoped>
.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 100px);
  background: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid #e4e7ed;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left i {
  font-size: 24px;
  color: #409eff;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: #f5f7fa;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  font-size: 16px;
  margin-bottom: 24px;
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
  max-width: 600px;
}

.quick-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.quick-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.message-item {
  display: flex;
  margin-bottom: 24px;
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  margin: 0 12px;
}

.message-item.user .message-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.message-item.assistant .message-avatar {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.message-content {
  max-width: 70%;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 12px;
  color: #909399;
}

.message-role {
  font-weight: 600;
}

.message-text {
  padding: 12px 16px;
  border-radius: 8px;
  line-height: 1.6;
  word-wrap: break-word;
}

.message-item.user .message-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message-item.assistant .message-text {
  background: white;
  color: #303133;
  border: 1px solid #e4e7ed;
  border-bottom-left-radius: 4px;
}

.message-text code {
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
}

.message-item.user .message-text code {
  background: rgba(255, 255, 255, 0.2);
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 16px !important;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409eff;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

.chat-input-area {
  padding: 16px 24px;
  background: white;
  border-top: 1px solid #e4e7ed;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.input-hint {
  font-size: 12px;
  color: #909399;
}

/* 滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message-content {
    max-width: 85%;
  }
  
  .quick-questions {
    max-width: 100%;
  }
  
  .chat-header {
    padding: 12px 16px;
  }
  
  .chat-messages {
    padding: 16px;
  }
}
</style>
