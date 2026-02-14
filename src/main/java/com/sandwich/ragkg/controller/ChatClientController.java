package com.sandwich.ragkg.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.sandwich.ragkg.advisor.SimpleMessageChatMemoryAdvisor;
import com.sandwich.ragkg.advisor.SwzAdvisor1;
import com.sandwich.ragkg.advisor.SwzAdvisor2;
import com.sandwich.ragkg.dao.entity.Book;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Consumer;

@RestController
@RequestMapping("chatClient")
public class ChatClientController {
    private final ChatClient chatClient;

    public ChatClientController(@Qualifier("dashScopeChatModel") ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    @GetMapping("/simple")
    public String chat(@RequestParam(name = "query") String query) throws GraphRunnerException {
        SystemMessage systemMessage = new SystemMessage("你是一个有用的AI科研助手，回答我一些科研问题");
        UserMessage userMessage = new UserMessage(query);
        DashScopeChatOptions dashScopeChatOptions = new DashScopeChatOptions();
        dashScopeChatOptions.setModel("qwen-plus");
        dashScopeChatOptions.setTemperature(0.0);
        dashScopeChatOptions.setMaxTokens(15536);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage), dashScopeChatOptions);
        return chatClient.prompt(prompt)
                .call()
                .content();
    }

    @GetMapping("/entity")
    public Book entity() throws GraphRunnerException {
        return chatClient.prompt()
                .user("请你随机生成一本书，要求书名和作者都是中文")
                .call()
                .entity(Book.class);
    }
    @GetMapping("/stream")
    public Flux<String> stream() throws GraphRunnerException {
        return chatClient.prompt()
                .user("请你随机生成一本书，要求书名和作者都是中文")
                .stream()
                .content();
    }

    @GetMapping("/advisor")
    public Book advisor() throws GraphRunnerException {
        return chatClient.prompt()
                .user("请你随机生成一本书，要求书名和作者都是中文")
                .advisors(new SwzAdvisor1(), new SwzAdvisor2())
                .call()
                .entity(Book.class);
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

}
