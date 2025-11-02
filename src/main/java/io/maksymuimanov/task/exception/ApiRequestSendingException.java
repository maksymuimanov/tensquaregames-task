package io.maksymuimanov.task.exception;

/**
 * Thrown when an asynchronous HTTP request cannot be successfully sent or retried.
 * <p>
 * Used by {@code RetryableAsyncApiRequestSender} to indicate that all retry
 * attempts have failed due to connection errors, timeouts, or non-success HTTP responses.
 *
 * @see io.maksymuimanov.task.api.AsyncApiRequestSender
 * @see io.maksymuimanov.task.api.RetryableAsyncApiRequestSender
 */
public class ApiRequestSendingException extends RuntimeException {
    /**
     * Creates a new exception with a descriptive message about the request failure.
     *
     * @param message details about why the request could not be sent or retried
     */
    public ApiRequestSendingException(String message) {
        super(message);
    }
}