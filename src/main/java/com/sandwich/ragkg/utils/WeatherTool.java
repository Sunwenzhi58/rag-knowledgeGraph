package com.sandwich.ragkg.utils;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.function.BiFunction;

// 定义天气查询工具
public class WeatherTool implements BiFunction<String, ToolContext, String> {
    @Override
    public String apply(
            @ToolParam(description = "The city name") String city,
            ToolContext toolContext) {
        return "It's always sunny in " + city + "!";
    }
//    @Override
//    public String apply(String city, ToolContext toolContext) {
//        return "It's always sunny in " + city + "!";
//    }

}