package io.maksymuimanov.task.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record DashboardResponse(JsonNode weather, JsonNode fact, JsonNode ip) {
}
