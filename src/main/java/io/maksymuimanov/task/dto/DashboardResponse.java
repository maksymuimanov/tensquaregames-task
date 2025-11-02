package io.maksymuimanov.task.dto;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents the aggregated dashboard response combining multiple
 * asynchronous API results into a single JSON payload.
 * <p>
 * Contains weather data, a random fact, and the client’s public IP address,
 * all fetched concurrently by the aggregator service.
 *
 * @param weather the JSON node containing current weather information
 * @param fact the JSON node containing a random fact
 * @param ip the JSON node containing the client’s IP address
 */
public record DashboardResponse(JsonNode weather, JsonNode fact, JsonNode ip) {
}