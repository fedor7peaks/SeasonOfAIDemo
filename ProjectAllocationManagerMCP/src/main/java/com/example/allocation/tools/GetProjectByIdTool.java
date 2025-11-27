package com.example.allocation.tools;

import com.example.allocation.model.Project;
import com.example.allocation.service.AllocationService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class GetProjectByIdTool {

    private final AllocationService allocationService;

    public GetProjectByIdTool(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @Tool(description = "Get a project by its ID")
    public Project GetProjectById(
            @ToolParam(description = "The ID of the project to retrieve") String projectId) {
        return allocationService.getProjectById(projectId);
    }
}
