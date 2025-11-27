package com.example.allocation.tools;

import com.example.allocation.model.Engineer;
import com.example.allocation.service.AllocationService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class GetEngineerByIdTool {

    private final AllocationService allocationService;

    public GetEngineerByIdTool(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @Tool(description = "Get an engineer by their ID")
    public Engineer GetEngineerById(
            @ToolParam(description = "The ID of the engineer to retrieve") String engineerId) {
        return allocationService.getEngineerById(engineerId);
    }
}
