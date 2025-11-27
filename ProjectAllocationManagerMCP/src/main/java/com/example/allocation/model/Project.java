package com.example.allocation.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Project(
        @JsonProperty("id")
        String id,
        @JsonProperty("name")
        String name,
        @JsonProperty("description")
        String description,
        @JsonProperty("status")
        String status
) {
}
