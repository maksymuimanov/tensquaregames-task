package io.maksymuimanov.task.exception;

/**
 * Represents an error that occurs while processing responses from external
 * HTTP APIs in the asynchronous aggregator. Typically wraps issues such as
 * invalid response parsing, unexpected status codes, or JSON deserialization failures.
 *
 * @see io.maksymuimanov.task.endpoint.AsyncHttpEndpointProcessor
 * @see io.maksymuimanov.task.endpoint.DashboardGetAsyncHttpEndpointProcessor
 */
public class HttpEndpointProcessionException extends RuntimeException {
    /**
     * Constructs a new HttpEndpointProcessionException with the given cause.
     * Used when a non-blocking HTTP response cannot be properly processed.
     *
     * @param cause the underlying exception that triggered the processing failure
     */
    public HttpEndpointProcessionException(Throwable cause) {
        super(cause);
    }
}