package com.example.weather;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class WeatherPrompts {

    @Bean
    public McpServerFeatures.SyncPromptSpecification newYorkWeatherPrompt() {
        var prompt = new McpSchema.Prompt(
                "NewYorkWeather",
                "Get weather forecast and alerts for New York City",
                null);

        return new McpServerFeatures.SyncPromptSpecification(prompt, (exchange, getPromptRequest) -> {
            var userMessage = new McpSchema.PromptMessage(
                    McpSchema.Role.USER,
                    new McpSchema.TextContent(
                            "Get the weather forecast for New York City (latitude: 40.7128, longitude: -74.0060) and check for any weather alerts in New York state."));

            return new McpSchema.GetPromptResult(
                    "Weather forecast and alerts for New York City",
                    List.of(userMessage));
        });
    }

    @Bean
    public McpServerFeatures.SyncPromptSpecification losAngelesWeatherPrompt() {
        var prompt = new McpSchema.Prompt(
                "LosAngelesWeather",
                "Get weather forecast and alerts for Los Angeles",
                null);

        return new McpServerFeatures.SyncPromptSpecification(prompt, (exchange, getPromptRequest) -> {
            var userMessage = new McpSchema.PromptMessage(
                    McpSchema.Role.USER,
                    new McpSchema.TextContent(
                            "Get the weather forecast for Los Angeles (latitude: 34.0522, longitude: -118.2437) and check for any weather alerts in California."));

            return new McpSchema.GetPromptResult(
                    "Weather forecast and alerts for Los Angeles",
                    List.of(userMessage));
        });
    }

    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> allPromptSpecifications() {
        return List.of(newYorkWeatherPrompt(), losAngelesWeatherPrompt());
    }
}
