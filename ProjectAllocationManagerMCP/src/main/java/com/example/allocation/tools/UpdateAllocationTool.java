package com.example.allocation.tools;

import com.example.allocation.service.AllocationService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class UpdateAllocationTool {

    private final AllocationService allocationService;

    public UpdateAllocationTool(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @Tool(description = "Update an existing allocation with new percentage and/or date range")
    public String UpdateAllocation(
            @ToolParam(description = "The ID of the allocation to update") String allocationId,
            @ToolParam(description = "The new allocation percentage (1-100), optional") Integer allocationPercentage,
            @ToolParam(description = "The new start date in YYYY-MM-DD format, optional") String startDate,
            @ToolParam(description = "The new end date in YYYY-MM-DD format, optional") String endDate) {

        AllocationService.AllocationResult result = allocationService.updateAllocation(
                allocationId, allocationPercentage, startDate, endDate);

        return result.message();
    }
}
