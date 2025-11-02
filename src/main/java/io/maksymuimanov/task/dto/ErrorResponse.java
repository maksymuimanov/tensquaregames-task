package io.maksymuimanov.task.dto;

/**
 * Represents a standardized JSON error response returned by the server.
 * <p>
 * Used to communicate human-readable error messages to clients when
 * endpoint processing or external API aggregation fails.
 *
 * @param message the error description to include in the HTTP response body
 */
public record ErrorResponse(String message) {
}