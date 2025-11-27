package com.example.allocation.tools;

import com.example.allocation.model.Engineer;
import com.example.allocation.service.AllocationService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListEngineersTool {

    private final AllocationService allocationService;

    public ListEngineersTool(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @Tool(description = "List all engineers in the system")
    public List<Engineer> ListEngineers() {
        return allocationService.getEngineers();
    }
}
