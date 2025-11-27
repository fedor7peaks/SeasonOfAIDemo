package com.example.allocation.tools;

import com.example.allocation.service.AllocationService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class AllocateEngineerTool {

    private final AllocationService allocationService;

    public AllocateEngineerTool(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @Tool(description = "Allocate an engineer to a project with a specified percentage and date range")
    public String AllocateEngineer(
            @ToolParam(description = "The ID of the engineer to allocate") String engineerId,
            @ToolParam(description = "The ID of the project to allocate the engineer to") String projectId,
            @ToolParam(description = "The allocation percentage (1-100)") int allocationPercentage,
            @ToolParam(description = "The start date in YYYY-MM-DD format (optional, defaults to today)") String startDate,
            @ToolParam(description = "The end date in YYYY-MM-DD format (optional, leave empty for indefinite)") String endDate) {

        AllocationService.AllocationResult result = allocationService.allocateEngineer(
                engineerId, projectId, allocationPercentage, startDate, endDate);

        if (result.success() && result.allocation() != null) {
            return result.message() + "\nAllocation ID: " + result.allocation().id();
        }
        return result.message();
    }
}
