package com.example.allocation.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Engineer(
        @JsonProperty("id")
        String id,
        @JsonProperty("name")
        String name,
        @JsonProperty("role")
        String role,
        @JsonProperty("skills")
        List<String> skills
) {
}
