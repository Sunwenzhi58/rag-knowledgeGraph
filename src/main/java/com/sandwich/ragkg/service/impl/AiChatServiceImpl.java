package com.sandwich.ragkg.service.impl;

import com.sandwich.ragkg.service.AiChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 对话服务实现类
 */
@Service
public class AiChatServiceImpl implements AiChatService {

    private static final Logger logger = LoggerFactory.getLogger(AiChatServiceImpl.class);

    @Autowired
    @Qualifier("dashScopeChatModel")
    private ChatModel chatModel;

    // 存储会话历史，key: sessionId, value: 消息列表
    private final Map<String, List<Message>> sessionHistory = new ConcurrentHashMap<>();

    // 系统提示词
    private static final String SYSTEM_PROMPT = """
            你是知识库AI助手，一个友好、专业的 AI 助手。
            请遵循以下规则：
            1. 使用简体中文回答
            2. 回答要清晰、准确、有条理
            3. 如果不确定，请诚实说明
            4. 保持对话的连贯性和上下文理解
            """;

    @Override
    public void streamChat(String message, String sessionId, SseEmitter emitter) {
        try {
            // 获取或创建会话历史
            List<Message> history = getOrCreateSession(sessionId);
            
            // 添加用户消息
            UserMessage userMessage = new UserMessage(message);
            history.add(userMessage);
            
            // 构建提示，包含历史记录
            List<Message> promptMessages = new ArrayList<>();
            promptMessages.add(new SystemMessage(SYSTEM_PROMPT));
            promptMessages.addAll(history);
            
            Prompt prompt = new Prompt(promptMessages);
            
            // 流式调用
            Flux<String> flux = chatModel.stream(prompt)
                    .map(response -> {
                        if (response.getResult() != null && 
                            response.getResult().getOutput() != null) {
                            return response.getResult().getOutput().getText();
                        }
                        return "";
                    });
            
            // 累积完整响应
            StringBuilder fullResponse = new StringBuilder();
            
            // 订阅流并发送 SSE 事件
            flux.subscribe(
                    chunk -> {
                        try {
                            if (chunk != null && !chunk.isEmpty()) {
                                fullResponse.append(chunk);
                                emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(chunk));
                            }
                        } catch (IOException e) {
                            logger.error("发送 SSE 消息失败", e);
                            emitter.completeWithError(e);
                        }
                    },
                    error -> {
                        logger.error("流式对话出错", error);
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("error")
                                    .data("对话出错：" + error.getMessage()));
                        } catch (IOException e) {
                            logger.error("发送错误消息失败", e);
                        }
                        emitter.completeWithError(error);
                    },
                    () -> {
                        try {
                            // 将 AI 的完整响应添加到历史记录
                            if (fullResponse.length() > 0) {
                                history.add(new AssistantMessage(fullResponse.toString()));
                            }
                            
                            // 发送结束事件
                            emitter.send(SseEmitter.event()
                                    .name("done")
                                    .data("[DONE]"));
                            emitter.complete();
                        } catch (IOException e) {
                            logger.error("完成 SSE 连接失败", e);
                            emitter.completeWithError(e);
                        }
                    }
            );
            
            // 设置超时和错误处理
            emitter.onTimeout(() -> {
                logger.warn("SSE 连接超时: sessionId={}", sessionId);
                emitter.complete();
            });
            
            emitter.onError(throwable -> {
                logger.error("SSE 连接错误: sessionId={}", sessionId, throwable);
            });
            
        } catch (Exception e) {
            logger.error("启动流式对话失败", e);
            emitter.completeWithError(e);
        }
    }

    @Override
    public void clearSession(String sessionId) {
        if (sessionId != null) {
            sessionHistory.remove(sessionId);
            logger.info("已清除会话历史: sessionId={}", sessionId);
        }
    }

    /**
     * 获取或创建会话
     */
    private List<Message> getOrCreateSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            // 如果没有 sessionId，返回新的临时会话
            return new ArrayList<>();
        }
        return sessionHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());
    }
}
