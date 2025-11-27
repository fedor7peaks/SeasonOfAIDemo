# Part 2: Building a Project Allocation Manager MCP Server

## Introduction

Imagine asking your AI assistant, "Which engineers with frontend skills are available next month?" and getting real-time answers from your actual project management system - not guesses, but live data from your organization's databases.

That's the power of the Model Context Protocol (MCP). MCP servers act as secure bridges, giving AI assistants direct access to your internal systems - databases, APIs, file systems, and more.

In this workshop, you'll build an MCP server for a project allocation system. While we're using JSON files for simplicity, the same patterns apply to any data source: SQL databases, REST APIs etc...

## Demo Video

[Watch this preview to see the Project Allocation Manager MCP in action](allocation-mcp-preview.mp4)

## Overview

**What's already provided:**
- `AllocationService` - Complete service with all business logic, validation and comprehensive documentation
- `Allocation`, `Engineer`, and `Project` model classes
- Sample data in JSON files
- Basic tool implementations: `ListEngineers`, `ListProjects`, `ListAllocations`

**What you'll build:**
- Tools to get individual records by ID
- Tools to allocate and update allocations
- Resources for commonly used data
- Prompts for common allocation tasks

## Getting Started

### Step 1: Understand the Existing Code

1. **Review the models** in `ProjectAllocationManagerMCP/src/main/java/com/example/allocation/model/`
   - `Allocation.java` - Allocation entity with dates
   - `Engineer.java` - Engineer entity with skills
   - `Project.java` - Project entity

2. **Check the sample data** in `ProjectAllocationManagerMCP/src/main/resources/data/`
   - `engineers.json` - 5 engineers with different skills
   - `projects.json` - 3 projects
   - `allocations.json` - 6 sample allocations

3. **Study the service** in `ProjectAllocationManagerMCP/src/main/java/com/example/allocation/service/AllocationService.java`
   - Read the JavaDoc comments on each method
   - Understand the available methods you can use
   - Note the complex validation logic for allocations

4. **Examine existing tools** in `ProjectAllocationManagerMCP/src/main/java/com/example/allocation/tool/AllocationTools.java`
   - See how `@Tool` annotations work
   - Understand how tools call service methods

### Step 2: Build the project

Build the ProjectAllocationManagerMCP project to ensure everything compiles correctly:

```bash
cd ProjectAllocationManagerMCP
mvn clean package
```

### Step 3: Configure MCP Server

To use your MCP server with GitHub Copilot in VS Code, configure it in the MCP settings file:

1. Create a `.vscode` folder in your project root if it doesn't exist
2. Create a file named `mcp.json` inside the `.vscode` folder
3. Add the following configuration:

```json
{
  "mcpServers": {
    "ProjectAllocationManagerMCP": {
      "command": "java",
      "args": [
        "-jar",
        "ProjectAllocationManagerMCP/target/allocation-mcp-1.0.0.jar"
      ]
    }
  }
}
```

**Alternative using Maven:**

```json
{
  "mcpServers": {
    "ProjectAllocationManagerMCP": {
      "command": "mvn",
      "args": [
        "-f",
        "ProjectAllocationManagerMCP/pom.xml",
        "spring-boot:run"
      ]
    }
  }
}
```

**What this does:**
- Defines an MCP server named "ProjectAllocationManagerMCP"
- Configures VS Code to run your server using `java -jar` or `mvn spring-boot:run`
- Points to your ProjectAllocationManagerMCP project

### Step 4: Test Existing Tools

Test the provided tools:
- Try listing all engineers
- Try listing all projects
- Try listing all active allocations

## Exercise Time 🚀

All exercises should be implemented in `AllocationTools.java` using the `@Tool` annotation pattern.

### Task 1: Create tool to retrieve engineers by id

**Service method to use:** `getEngineerById(String id)`

**Example implementation:**

```java
@Tool(description = "Get an engineer by their ID")
public Engineer GetEngineerById(
        @ToolParam(description = "The ID of the engineer to retrieve") String engineerId) {
    return allocationService.getEngineerById(engineerId);
}
```

### Task 2: Create tool to retrieve projects by id

**Service method to use:** `getProjectById(String id)`

**Hint:** Follow the same pattern as Task 1, but for projects.

### Task 3: Create tool to retrieve allocations by id

**Service method to use:** `getAllocationById(String id)`

**Hint:** Follow the same pattern as Task 1, but for allocations.

### Task 4: Create tool to allocate an engineer

**Service method to use:** `allocateEngineer(String engineerId, String projectId, int allocationPercentage, String startDate, String endDate)`

**Example implementation:**

```java
@Tool(description = "Allocate an engineer to a project with a specified percentage and date range")
public Map<String, Object> AllocateEngineer(
        @ToolParam(description = "The ID of the engineer to allocate") String engineerId,
        @ToolParam(description = "The ID of the project to allocate the engineer to") String projectId,
        @ToolParam(description = "The allocation percentage (1-100)") int allocationPercentage,
        @ToolParam(description = "The start date in YYYY-MM-DD format (optional, defaults to today)") String startDate,
        @ToolParam(description = "The end date in YYYY-MM-DD format (optional, leave empty for indefinite)") String endDate) {

    AllocationService.AllocationResult result = allocationService.allocateEngineer(
            engineerId, projectId, allocationPercentage, startDate, endDate);

    Map<String, Object> response = new HashMap<>();
    response.put("success", result.success());
    response.put("message", result.message());
    if (result.allocation() != null) {
        response.put("allocation", result.allocation());
    }
    return response;
}
```

### Task 5: Create tool to update allocation of an engineer

**Service method to use:** `updateAllocation(String allocationId, Integer allocationPercentage, String startDate, String endDate)`

**Hint:** Follow the same pattern as Task 4, but for updating allocations.

### Task 6: Add Reference Data Resources

Create resources that provide reference data to AI assistants.

**File to update:** `AllocationApplication.java`

**Resources to implement:**

1. **Engineers Resource:**

```java
@Bean
public McpServerFeatures.SyncResourceSpecification engineersResource(AllocationService allocationService) {
    var resource = new McpSchema.Resource(
            "allocation://engineers",
            "engineers.json",
            "Engineers List",
            "List all engineers with details",
            "application/json",
            null, null, null
    );

    return new McpServerFeatures.SyncResourceSpecification(resource, (exchange, request) -> {
        List<Engineer> engineers = allocationService.getEngineers();
        String jsonContent = new ObjectMapper().writeValueAsString(engineers);
        
        return new McpSchema.ReadResourceResult(
                List.of(new McpSchema.TextResourceContents(request.uri(), "application/json", jsonContent))
        );
    });
}
```

2. **Projects Resource:**

```java
@Bean
public McpServerFeatures.SyncResourceSpecification projectsResource(AllocationService allocationService) {
    var resource = new McpSchema.Resource(
            "allocation://projects",
            "projects.json",
            "Projects List",
            "List all projects with details",
            "application/json",
            null, null, null
    );

    return new McpServerFeatures.SyncResourceSpecification(resource, (exchange, request) -> {
        List<Project> projects = allocationService.getProjects();
        String jsonContent = new ObjectMapper().writeValueAsString(projects);
        
        return new McpSchema.ReadResourceResult(
                List.of(new McpSchema.TextResourceContents(request.uri(), "application/json", jsonContent))
        );
    });
}
```

Don't forget to register these resources:

```java
@Bean
public List<McpServerFeatures.SyncResourceSpecification> allResourceSpecifications(
        AllocationService allocationService) {
    return List.of(engineersResource(allocationService), projectsResource(allocationService));
}
```

### Task 7: Add Workflow Prompts

Create prompts that guide users through common tasks.

**File to update:** `AllocationApplication.java`

**Prompts to implement:**

1. **AllocateEngineerPrompt** - Guide users to allocate an engineer:

```java
@Bean
public McpServerFeatures.SyncPromptSpecification allocateEngineerPrompt() {
    var prompt = new McpSchema.Prompt(
            "AllocateEngineer",
            "Guide users to allocate an engineer to a project",
            List.of(
                new McpSchema.PromptArgument("engineerName", "Name of the engineer", true),
                new McpSchema.PromptArgument("projectName", "Name of the project", true),
                new McpSchema.PromptArgument("startDate", "Start date (YYYY-MM-DD)", false),
                new McpSchema.PromptArgument("endDate", "End date (YYYY-MM-DD)", false)
            )
    );

    return new McpServerFeatures.SyncPromptSpecification(prompt, (exchange, getPromptRequest) -> {
        String engineerName = (String) getPromptRequest.arguments().get("engineerName");
        String projectName = (String) getPromptRequest.arguments().get("projectName");
        String startDate = (String) getPromptRequest.arguments().getOrDefault("startDate", "today");
        String endDate = (String) getPromptRequest.arguments().getOrDefault("endDate", "indefinite");

        var userMessage = new McpSchema.PromptMessage(
                McpSchema.Role.USER,
                new McpSchema.TextContent(
                        String.format("Allocate engineer %s to project %s starting from %s until %s. " +
                                "First find the engineer and project IDs, then create the allocation.",
                                engineerName, projectName, startDate, endDate))
        );

        return new McpSchema.GetPromptResult(
                "Allocate engineer to project",
                List.of(userMessage)
        );
    });
}
```

2. **MoveEngineerToBenchPrompt** - Guide users to move an engineer to the bench:

```java
@Bean
public McpServerFeatures.SyncPromptSpecification moveEngineerToBenchPrompt() {
    var prompt = new McpSchema.Prompt(
            "MoveEngineerToBench",
            "Guide users to move an engineer to the bench (unallocate from current projects)",
            List.of(
                new McpSchema.PromptArgument("engineerName", "Name of the engineer", true)
            )
    );

    return new McpServerFeatures.SyncPromptSpecification(prompt, (exchange, getPromptRequest) -> {
        String engineerName = (String) getPromptRequest.arguments().get("engineerName");

        var userMessage = new McpSchema.PromptMessage(
                McpSchema.Role.USER,
                new McpSchema.TextContent(
                        String.format("Move engineer %s to the bench by finding their current allocations " +
                                "and updating them to end today. First find the engineer ID, then get their " +
                                "active allocations, and update each one to end today.",
                                engineerName))
        );

        return new McpSchema.GetPromptResult(
                "Move engineer to bench",
                List.of(userMessage)
        );
    });
}
```

Don't forget to register these prompts:

```java
@Bean
public List<McpServerFeatures.SyncPromptSpecification> allPromptSpecifications() {
    return List.of(allocateEngineerPrompt(), moveEngineerToBenchPrompt());
}
```

## Summary

By the end of this workshop, you'll have built:
- **8 Tools** for querying and updating allocations
- **2 Resources** providing reference data
- **2 Prompts** guiding common workflows

All implemented using Java Spring Boot with the Spring AI MCP server framework!
