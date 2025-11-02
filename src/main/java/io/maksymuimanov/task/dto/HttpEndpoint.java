package io.maksymuimanov.task.dto;

import io.netty.handler.codec.http.HttpMethod;

/**
 * Represents an immutable HTTP endpoint identified by its path and method.
 * <p>
 * Used as a key to map incoming HTTP requests to corresponding asynchronous
 * endpoint processors within the Netty-based API aggregator server.
 *
 * @param path the request URI path (e.g., "/api/dashboard")
 * @param method the HTTP method associated with the endpoint (e.g., GET, POST)
 */
public record HttpEndpoint(String path, HttpMethod method) {
}