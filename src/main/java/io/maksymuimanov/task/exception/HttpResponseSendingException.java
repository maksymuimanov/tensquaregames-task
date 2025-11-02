package io.maksymuimanov.task.exception;

/**
 * Thrown when sending an HTTP response to the client fails in the
 * asynchronous API aggregator. This can occur due to network errors,
 * client disconnects, or I/O failures during non-blocking response transmission.
 *
 * @see io.maksymuimanov.task.endpoint.HttpResponseSender
 * @see io.maksymuimanov.task.endpoint.JsonHttpResponseSender
 */
public class HttpResponseSendingException extends RuntimeException {
    /**
     * Constructs a new HttpResponseSendingException with the specified cause.
     *
     * @param cause the underlying exception that prevented the HTTP response from being sent
     */
    public HttpResponseSendingException(Throwable cause) {
        super(cause);
    }
}