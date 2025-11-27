package com.example.weather;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class WeatherResources {

    @Bean
    public McpServerFeatures.SyncResourceSpecification stateCodesResource() {
        var resource = new McpSchema.Resource(
                "weather://state-codes",
                "state-codes.json",
                "US State Codes",
                "List of US state codes and names for weather alerts",
                "application/json",
                null,
                null,
                null);

        return new McpServerFeatures.SyncResourceSpecification(resource, (exchange, request) -> {
            String jsonContent = """
                    {
                        "description": "US State codes for use with GetAlerts tool",
                        "states": [
                            {"code": "AL", "name": "Alabama"},
                            {"code": "AK", "name": "Alaska"},
                            {"code": "CA", "name": "California"},
                            {"code": "NY", "name": "New York"},
                            {"code": "TX", "name": "Texas"},
                            {"code": "FL", "name": "Florida"},
                            {"code": "IL", "name": "Illinois"},
                            {"code": "WA", "name": "Washington"}
                        ]
                    }
                    """;

            return new McpSchema.ReadResourceResult(
                    List.of(new McpSchema.TextResourceContents(request.uri(), "application/json", jsonContent)));
        });
    }

    @Bean
    public McpServerFeatures.SyncResourceSpecification majorCitiesResource() {
        var resource = new McpSchema.Resource(
                "weather://majorcities-coords",
                "majorcities-coords.json",
                "Major US Cities Coordinates",
                "Coordinates for major US cities to use with weather forecast",
                "application/json",
                null,
                null,
                null);

        return new McpServerFeatures.SyncResourceSpecification(resource, (exchange, request) -> {
            String jsonContent = """
                    {
                        "description": "Pre-defined coordinates for major US cities",
                        "cities": [
                            {"name": "New York, NY", "latitude": 40.7128, "longitude": -74.0060},
                            {"name": "Los Angeles, CA", "latitude": 34.0522, "longitude": -118.2437},
                            {"name": "Chicago, IL", "latitude": 41.8781, "longitude": -87.6298},
                            {"name": "Houston, TX", "latitude": 29.7604, "longitude": -95.3698}
                        ]
                    }
                    """;

            return new McpSchema.ReadResourceResult(
                    List.of(new McpSchema.TextResourceContents(request.uri(), "application/json", jsonContent)));
        });
    }

    @Bean
    public List<McpServerFeatures.SyncResourceSpecification> allResourceSpecifications() {
        return List.of(stateCodesResource(), majorCitiesResource());
    }
}
