package com.example.allocation.service;

import com.example.allocation.model.Allocation;
import com.example.allocation.model.Engineer;
import com.example.allocation.model.Project;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AllocationService {

    private final List<Engineer> engineers = new ArrayList<>();
    private final List<Project> projects = new ArrayList<>();
    private final List<Allocation> allocations = new ArrayList<>();
    private final String dataFolder;

    public AllocationService() {
        this.dataFolder = "data";
    }

    public AllocationService(String dataFolder) {
        this.dataFolder = dataFolder != null ? dataFolder : "data";
    }

    // Result record for allocation operations
    public record AllocationResult(boolean success, String message, Allocation allocation) {
    }

    // --- CRUD Methods ---

    public List<Engineer> getEngineers() {
        return engineers;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public List<Allocation> getAllocations() {
        return allocations;
    }

    public Engineer getEngineerById(String id) {
        return engineers.stream()
                .filter(e -> e.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Project getProjectById(String id) {
        return projects.stream()
                .filter(p -> p.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Allocation getAllocationById(String id) {
        return allocations.stream()
                .filter(a -> a.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Allocation> getAllocationsByEngineerId(String engineerId) {
        return allocations.stream()
                .filter(a -> a.engineerId().equals(engineerId))
                .collect(Collectors.toList());
    }

    public List<Allocation> getAllocationsByProjectId(String projectId) {
        return allocations.stream()
                .filter(a -> a.projectId().equals(projectId))
                .collect(Collectors.toList());
    }

    // --- Business Logic Methods ---

    public AllocationResult allocateEngineer(
            String engineerId,
            String projectId,
            int allocationPercentage,
            String startDate,
            String endDate) {

        // Validation 1: Check if engineer exists
        Engineer engineer = getEngineerById(engineerId);
        if (engineer == null) {
            return new AllocationResult(false,
                    String.format("Engineer with ID '%s' not found.", engineerId), null);
        }

        // Validation 2: Check if project exists
        Project project = getProjectById(projectId);
        if (project == null) {
            return new AllocationResult(false,
                    String.format("Project with ID '%s' not found.", projectId), null);
        }

        // Validation 3: Validate allocation percentage (must be between 1 and 100)
        if (allocationPercentage < 1 || allocationPercentage > 100) {
            return new AllocationResult(false,
                    "Allocation percentage must be between 1 and 100.", null);
        }

        // Validation 4: Validate and set dates
        LocalDateTime parsedStartDate;
        if (startDate == null || startDate.trim().isEmpty()) {
            parsedStartDate = LocalDate.now().atStartOfDay();
        } else {
            try {
                parsedStartDate = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
            } catch (DateTimeParseException e) {
                return new AllocationResult(false,
                        String.format("Invalid start date format: '%s'.", startDate), null);
            }
        }

        LocalDateTime parsedEndDate = null;
        if (endDate != null && !endDate.trim().isEmpty()) {
            try {
                parsedEndDate = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
            } catch (DateTimeParseException e) {
                return new AllocationResult(false,
                        String.format("Invalid end date format: '%s'.", endDate), null);
            }

            if (!parsedEndDate.isAfter(parsedStartDate)) {
                return new AllocationResult(false,
                        "End date must be after start date.", null);
            }
        }

        // Validation 5: Check for overlapping allocations and total percentage
        List<Allocation> existingAllocations = getAllocationsByEngineerId(engineerId);
        LocalDateTime finalParsedEndDate = parsedEndDate;
        List<Allocation> overlappingAllocations = existingAllocations.stream()
                .filter(a -> datesOverlap(parsedStartDate, finalParsedEndDate, a.startDate(), a.endDate()))
                .toList();

        if (!overlappingAllocations.isEmpty()) {
            int totalAllocation = overlappingAllocations.stream()
                    .mapToInt(Allocation::allocationPercentage)
                    .sum() + allocationPercentage;

            if (totalAllocation > 100) {
                int currentAllocation = overlappingAllocations.stream()
                        .mapToInt(Allocation::allocationPercentage)
                        .sum();
                return new AllocationResult(false,
                        String.format("Engineer '%s' is over-allocated. " +
                                        "Current allocation during this period: %d%%. " +
                                        "Adding %d%% would result in %d%% total allocation.",
                                engineer.name(), currentAllocation, allocationPercentage, totalAllocation),
                        null);
            }
        }

        // Validation 6: Check if engineer is already allocated to the same project with
        // overlapping dates
        Allocation duplicateAllocation = overlappingAllocations.stream()
                .filter(a -> a.projectId().equals(projectId))
                .findFirst()
                .orElse(null);

        if (duplicateAllocation != null) {
            String endDateStr = duplicateAllocation.endDate() != null
                    ? duplicateAllocation.endDate().toLocalDate().toString()
                    : "indefinite";
            return new AllocationResult(false,
                    String.format("Engineer '%s' is already allocated to project '%s' from %s to %s.",
                            engineer.name(), project.name(),
                            duplicateAllocation.startDate().toLocalDate().toString(), endDateStr),
                    null);
        }

        // Create new allocation
        Allocation newAllocation = new Allocation(
                "alloc-" + UUID.randomUUID().toString().substring(0, 8),
                engineerId,
                projectId,
                allocationPercentage,
                parsedStartDate,
                parsedEndDate);

        allocations.add(newAllocation);

        String message = parsedEndDate == null
                ? String.format("Successfully allocated %d%% of %s to %s starting from %s (indefinite).",
                allocationPercentage, engineer.name(), project.name(), parsedStartDate.toLocalDate())
                : String.format("Successfully allocated %d%% of %s to %s from %s to %s.",
                allocationPercentage, engineer.name(), project.name(),
                parsedStartDate.toLocalDate(), parsedEndDate.toLocalDate());

        return new AllocationResult(true, message, newAllocation);
    }

    public AllocationResult updateAllocation(
            String allocationId,
            Integer allocationPercentage,
            String startDate,
            String endDate) {

        // Validation 1: Find the allocation
        Allocation allocation = getAllocationById(allocationId);
        if (allocation == null) {
            return new AllocationResult(false,
                    String.format("Allocation with ID '%s' not found.", allocationId), null);
        }

        // Get engineer and project details for validation and messaging
        Engineer engineer = getEngineerById(allocation.engineerId());
        Project project = getProjectById(allocation.projectId());

        if (engineer == null || project == null) {
            return new AllocationResult(false,
                    "Associated engineer or project not found.", null);
        }

        // Parse and validate new dates
        LocalDateTime parsedStartDate = allocation.startDate();
        LocalDateTime parsedEndDate = allocation.endDate();

        if (startDate != null && !startDate.trim().isEmpty()) {
            try {
                parsedStartDate = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
            } catch (DateTimeParseException e) {
                return new AllocationResult(false,
                        String.format("Invalid start date format: '%s'.", startDate), null);
            }
        }

        if (endDate != null && !endDate.trim().isEmpty()) {
            try {
                parsedEndDate = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
            } catch (DateTimeParseException e) {
                return new AllocationResult(false,
                        String.format("Invalid end date format: '%s'.", endDate), null);
            }
        }

        // Validate end date is after start date
        if (parsedEndDate != null && !parsedEndDate.isAfter(parsedStartDate)) {
            return new AllocationResult(false,
                    "End date must be after start date.", null);
        }

        // Validate allocation percentage if provided
        int newAllocationPercentage = allocation.allocationPercentage();
        if (allocationPercentage != null) {
            if (allocationPercentage < 1 || allocationPercentage > 100) {
                return new AllocationResult(false,
                        "Allocation percentage must be between 1 and 100.", null);
            }
            newAllocationPercentage = allocationPercentage;
        }

        // Check for overlapping allocations (excluding the current allocation being
        // updated)
        List<Allocation> existingAllocations = getAllocationsByEngineerId(allocation.engineerId())
                .stream()
                .filter(a -> !a.id().equals(allocationId))
                .toList();

        LocalDateTime finalParsedEndDate = parsedEndDate;
        LocalDateTime finalParsedStartDate = parsedStartDate;
        List<Allocation> overlappingAllocations = existingAllocations.stream()
                .filter(a -> datesOverlap(finalParsedStartDate, finalParsedEndDate, a.startDate(), a.endDate()))
                .toList();

        if (!overlappingAllocations.isEmpty()) {
            int totalAllocation = overlappingAllocations.stream()
                    .mapToInt(Allocation::allocationPercentage)
                    .sum() + newAllocationPercentage;

            if (totalAllocation > 100) {
                int currentAllocation = overlappingAllocations.stream()
                        .mapToInt(Allocation::allocationPercentage)
                        .sum();
                return new AllocationResult(false,
                        String.format("Engineer '%s' would be over-allocated. " +
                                        "Current allocation during this period: %d%%. " +
                                        "Adding %d%% would result in %d%% total allocation.",
                                engineer.name(), currentAllocation, newAllocationPercentage, totalAllocation),
                        null);
            }
        }

        // Check for duplicate allocation to the same project (excluding current
        // allocation)
        Allocation duplicateAllocation = overlappingAllocations.stream()
                .filter(a -> a.projectId().equals(allocation.projectId()))
                .findFirst()
                .orElse(null);

        if (duplicateAllocation != null) {
            String endDateStr = duplicateAllocation.endDate() != null
                    ? duplicateAllocation.endDate().toLocalDate().toString()
                    : "indefinite";
            return new AllocationResult(false,
                    String.format("Engineer '%s' is already allocated to project '%s' " +
                                    "from %s to %s in allocation '%s'.",
                            engineer.name(), project.name(),
                            duplicateAllocation.startDate().toLocalDate().toString(), endDateStr,
                            duplicateAllocation.id()),
                    null);
        }

        // Update the allocation by creating a new record and replacing it
        Allocation updatedAllocation = new Allocation(
                allocation.id(),
                allocation.engineerId(),
                allocation.projectId(),
                newAllocationPercentage,
                parsedStartDate,
                parsedEndDate);

        // Replace the old allocation with the updated one
        allocations.remove(allocation);
        allocations.add(updatedAllocation);

        String message = parsedEndDate == null
                ? String.format(
                "Successfully updated allocation. %s is now %d%% allocated to %s starting from %s (indefinite).",
                engineer.name(), newAllocationPercentage, project.name(), parsedStartDate.toLocalDate())
                : String.format("Successfully updated allocation. %s is now %d%% allocated to %s from %s to %s.",
                engineer.name(), newAllocationPercentage, project.name(),
                parsedStartDate.toLocalDate(), parsedEndDate.toLocalDate());

        return new AllocationResult(true, message, updatedAllocation);
    }

    // Helper method to check if two date ranges overlap
    private boolean datesOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {

        // Case 1: Both have end dates
        if (end1 != null && end2 != null) {
            return start1.isBefore(end2) && end1.isAfter(start2);
        }
        // Case 2: First allocation is indefinite
        else if (end1 == null && end2 != null) {
            return start1.isBefore(end2);
        }
        // Case 3: Second allocation is indefinite
        else if (end1 != null && end2 == null) {
            return end1.isAfter(start2);
        }
        // Case 4: Both are indefinite
        else {
            return true; // Always overlaps
        }
    }

    // --- Data Loading ---

    @PostConstruct
    public void loadData() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(com.fasterxml.jackson.databind.DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

        // Load Engineers from classpath
        try (var engineersStream = getClass().getClassLoader().getResourceAsStream("data/engineers.json")) {
            if (engineersStream != null) {
                List<Engineer> loadedEngineers = objectMapper.readValue(
                        engineersStream, new TypeReference<>() {
                        });
                engineers.addAll(loadedEngineers);
            }
        }

        // Load Projects from classpath
        try (var projectsStream = getClass().getClassLoader().getResourceAsStream("data/projects.json")) {
            if (projectsStream != null) {
                List<Project> loadedProjects = objectMapper.readValue(
                        projectsStream, new TypeReference<>() {
                        });
                projects.addAll(loadedProjects);
            }
        }

        // Load Allocations from classpath
        try (var allocationsStream = getClass().getClassLoader().getResourceAsStream("data/allocations.json")) {
            if (allocationsStream != null) {
                List<Allocation> loadedAllocations = objectMapper.readValue(
                        allocationsStream, new TypeReference<>() {
                        });
                allocations.addAll(loadedAllocations);
            }
        }
    }
}
