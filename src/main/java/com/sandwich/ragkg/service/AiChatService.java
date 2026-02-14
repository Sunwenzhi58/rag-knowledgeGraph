package com.sandwich.ragkg.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 对话服务接口
 */
public interface AiChatService {
    
    /**
     * 流式对话
     * @param message 用户消息
     * @param sessionId 会话ID
     * @param emitter SSE发射器
     */
    void streamChat(String message, String sessionId, SseEmitter emitter);
    
    /**
     * 清除会话历史
     * @param sessionId 会话ID
     */
    void clearSession(String sessionId);
}
