package com.sandwich.ragkg.controller;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.sandwich.ragkg.common.Result;
import com.sandwich.ragkg.dto.resp.ResponseFormat;
import com.sandwich.ragkg.utils.JwtUtils;
import com.sandwich.ragkg.utils.UserLocationTool;
import com.sandwich.ragkg.utils.WeatherTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatModel chatModel;
    @Autowired
    JwtUtils jwtUtils;

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    String SYSTEM_PROMPT = """
    You are an expert weather forecaster, who speaks in puns.

    You have access to two tools:

    - get_weather_for_location: use this to get the weather for a specific location
    - get_user_location: use this to get the user's location

    If a user asks you for the weather, make sure you know the location.
    If you can tell from the question that they mean wherever they are,
    use the get_user_location tool to find their location.
    """;

    public ChatController(@Qualifier("dashScopeChatModel") ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(name = "query") String query) throws GraphRunnerException {
        return chatModel.call(query);
    }

    @GetMapping("/stream")
    public Flux<String> chatStream(@RequestParam(name = "query") String query) throws GraphRunnerException {
        return chatModel.stream(query);
    }
    @GetMapping("/message")
    public Flux<String> chatMessage(@RequestParam(name = "query") String query) throws GraphRunnerException {
        SystemMessage systemMessage = new SystemMessage("你是一个有用的AI助手");
        UserMessage userMessage = new UserMessage(query);
        return chatModel.stream(systemMessage, userMessage);
    }
    @GetMapping("/options")
    public String chatOptions(@RequestParam(name = "query") String query) throws GraphRunnerException {
        SystemMessage systemMessage = new SystemMessage("你是一个有用的AI科研助手，回答我一些科研问题");
        UserMessage userMessage = new UserMessage(query);
        DashScopeChatOptions dashScopeChatOptions = new DashScopeChatOptions();
        dashScopeChatOptions.setModel("qwen-plus");
        dashScopeChatOptions.setTemperature(0.0);
        dashScopeChatOptions.setMaxTokens(15536);
        ChatResponse response = chatModel.call(new Prompt(List.of(systemMessage, userMessage), dashScopeChatOptions));
        return response.getResult().getOutput().getText();
    }
    
//    @GetMapping("/alibaba/location")
//    public Result alibaba(@RequestParam(name = "location") String location) throws GraphRunnerException {
//        DashScopeApi dashScopeApi = DashScopeApi.builder()
//                .apiKey(apiKey)
//                .build();
//
//        ChatModel chatModel = DashScopeChatModel.builder()
//                .dashScopeApi(dashScopeApi)
//                .build();
//
//
//        ToolCallback weatherTool = FunctionToolCallback.builder("get_weather", new WeatherTool())
//                .description("Get weather for a given city")
//                .inputType(String.class)
//                .build();
//        // 创建 agent
//        ReactAgent agent = ReactAgent.builder()
//                .name("weather_agent")
//                .model(chatModel)
//                .tools(weatherTool)
//                .systemPrompt("You are a helpful assistant")
//                .saver(new MemorySaver())
//                .build();
//
//        // 运行 agent
//        AssistantMessage response = agent.call("what is the weather in" + location);
//        return Result.success(response.getText());
//    }
//
//    @GetMapping("/alibaba/location1")
//    public Result alibaba1(@RequestParam(name = "location1") String location1) throws GraphRunnerException {
//        DashScopeApi dashScopeApi = DashScopeApi.builder()
//                .apiKey(apiKey)
//                .build();
//
//        ChatModel chatModel = DashScopeChatModel.builder()
//                .dashScopeApi(dashScopeApi)
//                .defaultOptions(DashScopeChatOptions.builder()
//                        .withModel(DashScopeChatModel.DEFAULT_MODEL_NAME)
//                        .withTemperature(0.5)
//                        .withMaxToken(1000)
//                        .build())
//                .build();
//
//        ToolCallback weatherTool = FunctionToolCallback.builder("get_weather", new WeatherTool())
//                .description("Get weather for a given city")
//                .inputType(String.class)
//                .build();
//
//        ToolCallback getUserLocationTool = FunctionToolCallback
//                .builder("getUserLocation", new UserLocationTool())
//                .description("Retrieve user location based on user ID")
//                .inputType(String.class)
//                .build();
//        // 创建 agent
//        ReactAgent agent = ReactAgent.builder()
//                .name("weather_agent")
//                .model(chatModel)
//                .tools(weatherTool, getUserLocationTool)
//                .systemPrompt(SYSTEM_PROMPT)
//                .outputType(ResponseFormat.class)
//                .saver(new MemorySaver())
//                .build();
//
//        // 从 JWT token 中提取用户 ID
////        String userId = jwtUtils.extractTokenIdFromToken(token);
//        RunnableConfig runnableConfig = RunnableConfig.builder().threadId("1").build();
//
//        // 运行 agent
//        AssistantMessage response = agent.call("what is the weather in" + location1, runnableConfig);
//        return Result.success(response.getText());
//    }
}
