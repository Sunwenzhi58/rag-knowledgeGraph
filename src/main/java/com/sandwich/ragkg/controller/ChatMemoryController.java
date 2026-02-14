package com.sandwich.ragkg.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.sandwich.ragkg.advisor.SimpleMessageChatMemoryAdvisor;
import com.sandwich.ragkg.advisor.SwzAdvisor1;
import com.sandwich.ragkg.advisor.SwzAdvisor2;
import com.sandwich.ragkg.dao.entity.Book;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("chatMemory")
public class ChatMemoryController {
    private final ChatClient chatClient;

    public ChatMemoryController(@Qualifier("dashScopeChatModel") ChatModel chatModel) {
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .build();
        MessageChatMemoryAdvisor chatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory)
                .build();
        this.chatClient = ChatClient
                .builder(chatModel)
                .defaultAdvisors(chatMemoryAdvisor)
                .build();
    }


    @GetMapping("/simpleMessageChatMemoryAdvisor")
    public String simpleMessageChatMemoryAdvisor(@RequestParam(name = "query") String query,
                                                 @RequestParam(name = "conversationId") String conversationId) throws GraphRunnerException {
        return chatClient.prompt()
                .user(query)
                // 把会话id存入上下文
                .advisors(advisorSpec -> advisorSpec.param("conversationId", conversationId))
                .advisors(new SimpleMessageChatMemoryAdvisor())
                .call()
                .content();
    }


    @GetMapping("/messageChatMemoryAdvisor")
    public String messageChatMemoryAdvisor(@RequestParam(name = "query") String query,
                                           @RequestParam(name = "conversationId") String conversationId) throws GraphRunnerException {
        return chatClient.prompt()
                .user(query)
                // 把会话id存入上下文
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    public static void main(String[] args) {
        PromptTemplate userPrompt = new PromptTemplate("你是一个有用的人工智能助手，名字是{name}，请用{voice}的风格回答以下问题：{userQuestion}");
        Message message = userPrompt.createMessage(Map.of("name", "小智", "voice", "温柔", "userQuestion", "推荐北京的三个景点"));
        System.out.println(message);
    }

}
