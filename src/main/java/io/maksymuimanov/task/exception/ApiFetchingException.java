package io.maksymuimanov.task.exception;

/**
 * Thrown when an asynchronous HTTP request to an external API fails.
 * <p>
 * Used by {@code JsonAsyncApiFetcher} to wrap exceptions occurring during
 * network communication, JSON parsing, or timeout handling.
 *
 * @see io.maksymuimanov.task.api.AsyncApiFetcher
 * @see io.maksymuimanov.task.api.JsonAsyncApiFetcher
 */
public class ApiFetchingException extends RuntimeException {
    /**
     * Creates a new exception wrapping the underlying cause of the failed API fetch.
     *
     * @param cause the root cause of the fetching failure (e.g., timeout, parsing error)
     */
    public ApiFetchingException(Throwable cause) {
        super(cause);
    }
}