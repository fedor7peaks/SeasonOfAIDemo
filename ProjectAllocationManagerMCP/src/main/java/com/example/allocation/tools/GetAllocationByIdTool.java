package com.example.allocation.tools;

import com.example.allocation.model.Allocation;
import com.example.allocation.service.AllocationService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class GetAllocationByIdTool {

    private final AllocationService allocationService;

    public GetAllocationByIdTool(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @Tool(description = "Get an allocation by its ID")
    public Allocation GetAllocationById(
            @ToolParam(description = "The ID of the allocation to retrieve") String allocationId) {
        return allocationService.getAllocationById(allocationId);
    }
}
