package com.example.allocation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;

@JsonDeserialize
public record Allocation(
        @JsonProperty("id")
        String id,
        @JsonProperty("engineerId")
        String engineerId,
        @JsonProperty("projectId")
        String projectId,
        @JsonProperty("allocationPercentage")
        int allocationPercentage,
        @JsonProperty("startDate")
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime startDate,
        @JsonProperty("endDate")
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime endDate
) {
}
