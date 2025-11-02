package io.maksymuimanov.task.exception;

/**
 * Thrown when the initialization of a Netty HTTP socket channel fails in the
 * asynchronous API aggregator. This can occur due to low-level network issues,
 * misconfigured channel pipelines, or resource allocation failures during
 * non-blocking server startup.
 *
 * @see io.maksymuimanov.task.server.HttpSocketChannelInitializer
 */
public class HttpSocketChannelInitializingException extends RuntimeException {
    /**
     * Constructs a new HttpSocketChannelInitializingException with the specified cause.
     *
     * @param cause the underlying exception that prevented the HTTP socket channel from initializing
     */
    public HttpSocketChannelInitializingException(Throwable cause) {
        super(cause);
    }
}
