package com.example.allocation;

import com.example.allocation.tools.ListEngineersTool;
import com.example.allocation.tools.ListProjectsTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AllocationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AllocationApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider toolCallbackProvider(
            ListEngineersTool listEngineersTool,
            ListProjectsTool listProjectsTool) {
        return MethodToolCallbackProvider
                .builder()
                .toolObjects(
                        listEngineersTool,
                        listProjectsTool)
                .build();
    }
}
