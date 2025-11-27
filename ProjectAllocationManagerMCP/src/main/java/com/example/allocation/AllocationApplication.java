package com.example.allocation;

import com.example.allocation.tools.*;
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
            ListProjectsTool listProjectsTool,
            ListAllocationsTool listAllocationsTool,
            GetEngineerByIdTool getEngineerByIdTool,
            GetProjectByIdTool getProjectByIdTool,
            GetAllocationByIdTool getAllocationByIdTool,
            AllocateEngineerTool allocateEngineerTool,
            UpdateAllocationTool updateAllocationTool) {
        return MethodToolCallbackProvider
                .builder()
                .toolObjects(
                        listEngineersTool,
                        listProjectsTool,
                        listAllocationsTool,
                        getEngineerByIdTool,
                        getProjectByIdTool,
                        getAllocationByIdTool,
                        allocateEngineerTool,
                        updateAllocationTool)
                .build();
    }
}
