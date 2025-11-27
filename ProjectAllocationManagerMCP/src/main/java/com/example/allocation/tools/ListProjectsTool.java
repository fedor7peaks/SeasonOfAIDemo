package com.example.allocation.tools;

import com.example.allocation.model.Project;
import com.example.allocation.service.AllocationService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListProjectsTool {

    private final AllocationService allocationService;

    public ListProjectsTool(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @Tool(description = "List all projects in the system")
    public List<Project> ListProjects() {
        return allocationService.getProjects();
    }
}
