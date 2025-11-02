package io.maksymuimanov.task.exception;

/**
 * Signals an error occurred while directing or routing HTTP requests
 * within the asynchronous API aggregation flow. This exception typically
 * wraps issues like malformed URLs, unreachable endpoints, or failures
 * in request dispatching.
 *
 * @see io.maksymuimanov.task.endpoint.HttpEndpointDirector
 * @see io.maksymuimanov.task.endpoint.SimpleHttpEndpointDirector
 */
public class HttpEndpointDirectingException extends RuntimeException {
    /**
     * Constructs a new HttpEndpointDirectingException with the specified cause.
     * Used when a non-blocking HTTP request cannot be properly routed or executed.
     *
     * @param cause the underlying exception that triggered the routing failure
     */
    public HttpEndpointDirectingException(Throwable cause) {
        super(cause);
    }
}