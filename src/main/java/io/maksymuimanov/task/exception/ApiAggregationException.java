package io.maksymuimanov.task.exception;

/**
 * Thrown when an error occurs during asynchronous API aggregation.
 * <p>
 * Typically wraps failures from concurrent API fetches or data merging
 * in the {@code DashboardAsyncApiAggregator}.
 *
 * @see io.maksymuimanov.task.api.AsyncApiAggregator
 * @see io.maksymuimanov.task.api.DashboardAsyncApiAggregator
 */
public class ApiAggregationException extends RuntimeException {
    /**
     * Creates a new exception wrapping the original aggregation failure cause.
     *
     * @param cause the underlying exception that triggered the aggregation failure
     */
    public ApiAggregationException(Throwable cause) {
        super(cause);
    }
}