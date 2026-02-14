package com.sandwich.ragkg.controller;

import com.sandwich.ragkg.service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 对话 Controller - 基于 SSE 实现流式响应
 */
@RestController
@RequestMapping("/ai-chat")
@CrossOrigin(origins = "*")
public class AiChatController {

    @Autowired
    private AiChatService aiChatService;

    /**
     * SSE 流式对话接口
     * @param message 用户消息
     * @param sessionId 会话ID（可选，用于保持上下文）
     * @return SseEmitter
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(
            @RequestParam("message") String message,
            @RequestParam(value = "sessionId", required = false) String sessionId) {
        
        SseEmitter emitter = new SseEmitter(60000L); // 60秒超时
        
        aiChatService.streamChat(message, sessionId, emitter);
        
        return emitter;
    }

    /**
     * 清除会话历史
     * @param sessionId 会话ID
     */
    @DeleteMapping("/session/{sessionId}")
    public void clearSession(@PathVariable String sessionId) {
        aiChatService.clearSession(sessionId);
    }
}
