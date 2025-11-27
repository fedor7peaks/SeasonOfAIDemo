package com.example.weather;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WeatherTools {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(WeatherService weatherService) {
        return MethodToolCallbackProvider
                .builder()
                .toolObjects(weatherService)
                .build();
    }
}
