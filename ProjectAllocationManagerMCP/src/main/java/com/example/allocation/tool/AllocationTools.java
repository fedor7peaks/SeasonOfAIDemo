package com.example.allocation.tool;

import com.example.allocation.model.Allocation;
import com.example.allocation.model.Engineer;
import com.example.allocation.model.Project;
import com.example.allocation.service.AllocationService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AllocationTools {

    private final AllocationService allocationService;

    public AllocationTools(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @Tool(description = "List all engineers in the system")
    public List<Engineer> ListEngineers() {
        return allocationService.getEngineers();
    }

    @Tool(description = "List all projects in the system")
    public List<Project> ListProjects() {
        return allocationService.getProjects();
    }

    @Tool(description = "List all active allocations in the system (allocations that are currently ongoing)")
    public List<Allocation> ListAllocations() {
        List<Allocation> allAllocations = allocationService.getAllocations();
        LocalDate today = LocalDate.now();

        // Filter to only active allocations
        return allAllocations.stream()
                .filter(a -> {
                    // Check if allocation has started
                    if (a.startDate().toLocalDate().isAfter(today)) {
                        return false;
                    }
                    // Check if allocation has ended (null EndDate means indefinite)
                    if (a.endDate() != null && a.endDate().toLocalDate().isBefore(today)) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    @Tool(description = "Get an engineer by their ID")
    public Engineer GetEngineerById(
            @ToolParam(description = "The ID of the engineer to retrieve") String engineerId) {
        return allocationService.getEngineerById(engineerId);
    }

    @Tool(description = "Get a project by its ID")
    public Project GetProjectById(
            @ToolParam(description = "The ID of the project to retrieve") String projectId) {
        return allocationService.getProjectById(projectId);
    }

    @Tool(description = "Get an allocation by its ID")
    public Allocation GetAllocationById(
            @ToolParam(description = "The ID of the allocation to retrieve") String allocationId) {
        return allocationService.getAllocationById(allocationId);
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
