# Part 1: Building a Weather MCP Server

In this workshop, you'll build a Model Context Protocol (MCP) server that provides weather information through tools, resources, and prompts.

## Overview

You'll be working with a Java Spring MCP server that integrates with the National Weather Service API to provide weather forecasts and alerts. The `WeatherService` class is already implemented and handles the API calls.

**Note:** The National Weather Service API only provides data for US locations.

## Step 1: Register the MCP Server

The MCP server is already registered in `WeatherApplication.java`.

**File:** `WeatherMCP/src/main/java/com/example/weather/WeatherApplication.java`

The application includes:

```java
@Bean
public ToolCallbackProvider weatherTools(WeatherService weatherService) {
    return MethodToolCallbackProvider
            .builder()
            .toolObjects(weatherService)
            .build();
}
```

**What this does:**
- `@Bean` - Registers the tool callback provider as a Spring bean
- `MethodToolCallbackProvider` - Auto-discovers `@Tool` annotated methods in the service
- `toolObjects(weatherService)` - Registers all tools from the WeatherService

## Step 2: Add Weather Tools

Tools are functions that can be called by AI assistants to perform actions.

**File:** `WeatherMCP/src/main/java/com/example/weather/WeatherService.java`

The tools are already implemented using `@Tool` annotations:

1. **GetForecast** tool:

```java
@Tool(description = "Get weather forecast for a location")
public String GetForecast(
        @ToolParam(description = "The latitude of the location to get the forecast for.") double latitude,
        @ToolParam(description = "The longitude of the location to get the forecast for.") double longitude) {
    // Implementation using RestClient
}
```

2. **GetAlerts** tool:

```java
@Tool(description = "Get weather alerts for a US state")
public String GetAlerts(
        @ToolParam(description = "The US state to get alerts for (e.g., CA, NY, TX).") String state) {
    // Implementation using RestClient
}
```

**What this does:**
- `@Tool` - Marks the method as an MCP tool
- `@ToolParam` - Provides parameter descriptions to the AI
- The `RestClient` is used to call the National Weather Service API

## Step 2.1: Build the Project

Build the WeatherMCP project to ensure everything compiles correctly:

```bash
cd WeatherMCP
mvn clean package
```

## Step 2.2: Configure the MCP Server in VS Code

To use your MCP server with GitHub Copilot in VS Code, you need to configure it in the MCP settings file.

1. Create a `.vscode` folder in your project root if it doesn't exist
2. Create a file named `mcp.json` inside the `.vscode` folder
3. Add the following configuration:

```json
{
  "mcpServers": {
    "WeatherMCP": {
      "command": "java",
      "args": [
        "-jar",
        "WeatherMCP/target/weather-mcp-1.0.0.jar"
      ]
    }
  }
}
```

**What this does:**
- Defines an MCP server named "WeatherMCP"
- Configures VS Code to run your server using `java -jar`
- Points to your built JAR file

**Alternative using Maven:**

```json
{
  "mcpServers": {
    "WeatherMCP": {
      "command": "mvn",
      "args": [
        "-f",
        "WeatherMCP/pom.xml",
        "spring-boot:run"
      ]
    }
  }
}
```

## Step 2.3: Test Your Tools with GitHub Copilot

Now let's test that your tools are working!

1. **Restart the MCP server:**
   - Open `mcp.json` file
   - Click the "Restart" button next to "WeatherMCP"

2. **Open GitHub Copilot Chat** (Ctrl+Shift+I)

3. **Try these test prompts:**
   - "What's the weather forecast for New York City?"
   - "Are there any weather alerts in California?"
   - "Get the weather forecast for Chicago"

4. **Verify the tools are being called:**
   - You should see Copilot using the `GetForecast` and `GetAlerts` tools
   - The responses should include actual weather data from the National Weather Service

**Troubleshooting:**
- Check that `.vscode/mcp.json` is properly formatted
- Verify the project builds: `mvn clean package`
- Check the Output panel: View → Output → select "MCP"

### Alternative: Test with MCP Inspector

You can also test your MCP server using the MCP Inspector, which provides a web-based UI to interact with your server.

1. **Run the MCP Inspector:**
   ```bash
   npx @modelcontextprotocol/inspector mvn -f WeatherMCP/pom.xml spring-boot:run
   ```

2. **Open the Inspector:**
   - The command will output a URL (typically `http://localhost:5173`)
   - Open this URL in your browser

3. **Test your tools:**
   - You'll see a visual interface showing your available tools, resources, and prompts
   - Click on the "Tools" tab to see `GetAlerts` and `GetForecast`
   - Try calling the tools with different parameters
   - View the JSON responses from the National Weather Service API

**Benefits:**
- Visual debugging interface
- See exact JSON messages
- Test without client configuration

## Step 3: Add Weather Resources

Resources provide static or dynamic data that can be accessed by AI assistants.

**File:** `WeatherMCP/src/main/java/com/example/weather/WeatherApplication.java`

The resources are already implemented as Spring beans:

1. **State Codes Resource:**

```java
@Bean
public McpServerFeatures.SyncResourceSpecification stateCodesResource() {
    var resource = new McpSchema.Resource(
            "weather://state-codes",
            "state-codes.json",
            "US State Codes",
            "List of US state codes and names for weather alerts",
            "application/json",
            null, null, null
    );

    return new McpServerFeatures.SyncResourceSpecification(resource, (exchange, request) -> {
        String jsonContent = """
                {
                    "description": "US State codes for use with GetAlerts tool",
                    "states": [
                        {"code": "AL", "name": "Alabama"},
                        {"code": "AK", "name": "Alaska"},
                        {"code": "CA", "name": "California"},
                        {"code": "NY", "name": "New York"}
                    ]
                }
                """;
        return new McpSchema.ReadResourceResult(
                List.of(new McpSchema.TextResourceContents(request.uri(), "application/json", jsonContent))
        );
    });
}
```

2. **Major Cities Resource:**

```java
@Bean
public McpServerFeatures.SyncResourceSpecification majorCitiesResource() {
    var resource = new McpSchema.Resource(
            "weather://majorcities-coords",
            "majorcities-coords.json",
            "Major US Cities Coordinates",
            "Coordinates for major US cities to use with weather forecast",
            "application/json",
            null, null, null
    );

    return new McpServerFeatures.SyncResourceSpecification(resource, (exchange, request) -> {
        String jsonContent = """
                {
                    "description": "Pre-defined coordinates for major US cities",
                    "cities": [
                        {"name": "New York, NY", "latitude": 40.7128, "longitude": -74.0060},
                        {"name": "Los Angeles, CA", "latitude": 34.0522, "longitude": -118.2437},
                        {"name": "Chicago, IL", "latitude": 41.8781, "longitude": -87.6298},
                        {"name": "Houston, TX", "latitude": 29.7604, "longitude": -95.3698}
                    ]
                }
                """;
        return new McpSchema.ReadResourceResult(
                List.of(new McpSchema.TextResourceContents(request.uri(), "application/json", jsonContent))
        );
    });
}
```

**What this does:**
- `@Bean` - Registers the resource as a Spring bean
- `McpServerFeatures.SyncResourceSpecification` - Defines a synchronous resource
- `McpSchema.Resource` - Metadata about the resource (URI, name, description)
- Resources return JSON-serialized data that AI assistants can reference

## Step 3.1: Test Resources in GitHub Copilot

1. **Restart the MCP server:**
   - Open `mcp.json` file
   - Click the "Restart" button next to "WeatherMCP"

2. **Open GitHub Copilot Chat** (Ctrl+Shift+I)

3. **Add resources to your chat context:**
   - Click the **Add Context** icon (paperclip icon) in the chat input
   - Select **MCP Resources**
   - Choose `weather://state-codes` or `weather://majorcities-coords`

4. **Try questions that use the resources:**
   - "What state codes are available for weather alerts?"
   - "Show me the coordinates for major US cities"
   - "What's the weather in one of the major cities you know about?"

5. **Observe:**
   - Copilot can reference the resource data you added
   - It knows the pre-defined state codes and city coordinates

## Step 4: Add Weather Prompts

Prompts are pre-defined templates that help AI assistants perform common tasks.

**File:** `WeatherMCP/src/main/java/com/example/weather/WeatherApplication.java`

The prompts are already implemented as Spring beans:

1. **New York Weather Prompt:**

```java
@Bean
public McpServerFeatures.SyncPromptSpecification newYorkWeatherPrompt() {
    var prompt = new McpSchema.Prompt(
            "NewYorkWeather",
            "Get weather forecast and alerts for New York City",
            null
    );

    return new McpServerFeatures.SyncPromptSpecification(prompt, (exchange, getPromptRequest) -> {
        var userMessage = new McpSchema.PromptMessage(
                McpSchema.Role.USER,
                new McpSchema.TextContent(
                        "Get the weather forecast for New York City (latitude: 40.7128, longitude: -74.0060) and check for any weather alerts in New York state.")
        );

        return new McpSchema.GetPromptResult(
                "Weather forecast and alerts for New York City",
                List.of(userMessage)
        );
    });
}
```

2. **Los Angeles Weather Prompt:**

```java
@Bean
public McpServerFeatures.SyncPromptSpecification losAngelesWeatherPrompt() {
    var prompt = new McpSchema.Prompt(
            "LosAngelesWeather",
            "Get weather forecast and alerts for Los Angeles",
            null
    );

    return new McpServerFeatures.SyncPromptSpecification(prompt, (exchange, getPromptRequest) -> {
        var userMessage = new McpSchema.PromptMessage(
                McpSchema.Role.USER,
                new McpSchema.TextContent(
                        "Get the weather forecast for Los Angeles (latitude: 34.0522, longitude: -118.2437) and check for any weather alerts in California.")
        );

        return new McpSchema.GetPromptResult(
                "Weather forecast and alerts for Los Angeles",
                List.of(userMessage)
        );
    });
}
```

**What this does:**
- `@Bean` - Registers the prompt as a Spring bean
- `McpServerFeatures.SyncPromptSpecification` - Defines a synchronous prompt
- `McpSchema.Prompt` - Metadata about the prompt (name, description)
- Prompts return messages that provide instructions to AI assistants

## Step 4.1: Test Prompts in GitHub Copilot

1. **Restart the MCP server:**
   - Open `mcp.json` file
   - Click the "Restart" button next to "WeatherMCP"

2. **Open GitHub Copilot Chat** (Ctrl+Shift+I)

3. **Use the prompts:**
   - Type `/` in the chat input to see available prompts
   - Look for:
     - `/NewYorkWeather` - Quick weather check for New York
     - `/LosAngelesWeather` - Quick weather check for Los Angeles

4. **Try the prompts:**
   - Type `/NewYorkWeather` and press Enter
   - Type `/LosAngelesWeather` and press Enter

5. **Verify:**
   - The prompts automatically construct the right queries
   - Copilot uses your tools (`GetForecast` and `GetAlerts`) based on the prompt instructions
   - You get comprehensive weather information without typing detailed requests

**What's happening:**
- Prompts provide pre-written instructions that guide Copilot
- They combine with your tools and resources to create powerful, reusable workflows

## Additional Testing Options

Beyond GitHub Copilot, you can test your MCP server in other ways:

1. **MCP Inspector** (covered earlier) - Visual debugging interface
2. **Claude Desktop** - Configure the server in Claude's settings
3. **Other MCP-compatible clients** - Any client that supports the MCP protocol

## Summary

You've successfully created an MCP server with:
- **2 Tools**: `GetAlerts` and `GetForecast`
- **2 Resources**: State codes and major cities
- **2 Prompts**: New York weather and Los Angeles weather

These components work together to provide a comprehensive weather information service that AI assistants can use to help users get weather data.
