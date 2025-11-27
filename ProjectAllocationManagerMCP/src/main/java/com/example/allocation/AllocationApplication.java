package com.example.allocation;

import com.example.allocation.tool.AllocationTools;
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
    public ToolCallbackProvider toolCallbackProvider(AllocationTools allocationTools) {
        return MethodToolCallbackProvider
                .builder()
                .toolObjects(allocationTools)
                .build();
    }
}
