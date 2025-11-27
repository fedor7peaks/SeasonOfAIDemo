package com.example.allocation.tools;

import com.example.allocation.model.Allocation;
import com.example.allocation.service.AllocationService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ListAllocationsTool {

    private final AllocationService allocationService;

    public ListAllocationsTool(AllocationService allocationService) {
        this.allocationService = allocationService;
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
}
