package com.example.weather;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class WeatherService {

    private static final String BASE_URL = "https://api.weather.gov";

    private final RestClient restClient;

    public WeatherService() {
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("Accept", "application/geo+json")
                .defaultHeader("User-Agent", "weather-tool/1.0")
                .build();
    }

    // Record classes for JSON deserialization
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Points(@JsonProperty("properties") Props properties) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Props(@JsonProperty("forecast") String forecast) {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Forecast(@JsonProperty("properties") Props properties) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Props(@JsonProperty("periods") List<Period> periods) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Period(
                @JsonProperty("name") String name,
                @JsonProperty("temperature") Integer temperature,
                @JsonProperty("temperatureUnit") String temperatureUnit,
                @JsonProperty("shortForecast") String shortForecast) {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Alert(@JsonProperty("features") List<Feature> features) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Feature(@JsonProperty("properties") Properties properties) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Properties(
                @JsonProperty("event") String event,
                @JsonProperty("headline") String headline) {
        }
    }

    /**
     * Get weather forecast for a specific latitude/longitude
     *
     * @param latitude  Latitude of the location
     * @param longitude Longitude of the location
     * @return The forecast for the given location
     */
    @Tool(description = "Get weather forecast for a location")
    public String GetForecast(
            @ToolParam(description = "The latitude of the location to get the forecast for.") double latitude,
            @ToolParam(description = "The longitude of the location to get the forecast for.") double longitude) {
        try {
            // Get points data
            var points = restClient.get()
                    .uri("/points/{latitude},{longitude}", latitude, longitude)
                    .retrieve()
                    .body(Points.class);

            // Get forecast URL from points
            String forecastUrl = points.properties().forecast();

            // Get forecast data
            var forecast = restClient.get()
                    .uri(forecastUrl)
                    .retrieve()
                    .body(Forecast.class);

            // Format forecast text (first 3 periods)
            String forecastText = forecast.properties().periods().stream()
                    .limit(3)
                    .map(p -> String.format("%s: %d°%s, %s",
                            p.name(),
                            p.temperature(),
                            p.temperatureUnit(),
                            p.shortForecast()))
                    .collect(Collectors.joining("\n"));

            return String.format("Weather forecast for %s, %s:\n%s", latitude, longitude, forecastText);

        } catch (NumberFormatException ex) {
            return String.format("Error: Invalid latitude or longitude format. Please provide valid numbers.");
        } catch (RestClientException ex) {
            return String.format("Error retrieving forecast: %s", ex.getMessage());
        }
    }

    /**
     * Get weather alerts for a US state
     *
     * @param state Two-letter US state code (e.g., CA, NY, TX)
     * @return Human readable alert information
     */
    @Tool(description = "Get weather alerts for a US state")
    public String GetAlerts(
            @ToolParam(description = "The US state to get alerts for (e.g., CA, NY, TX).") String state) {
        try {
            Alert alert = restClient.get()
                    .uri("/alerts/active?area={state}", state)
                    .retrieve()
                    .body(Alert.class);

            if (alert.features().isEmpty()) {
                return String.format("No active weather alerts for %s", state);
            }

            String alertsText = alert.features().stream()
                    .map(f -> String.format("- %s: %s",
                            f.properties().event(),
                            f.properties().headline()))
                    .collect(Collectors.joining("\n"));

            return String.format("Active weather alerts for %s:\n%s", state, alertsText);

        } catch (RestClientException ex) {
            return String.format("Error retrieving alerts: %s. Try using a valid US state code (e.g., CA, NY, TX).",
                    ex.getMessage());
        }
    }
}
