package io.maksymuimanov.task.exception;

/**
 * Indicates a failure occurred while handling inbound HTTP requests on the
 * Netty server channel within the asynchronous API aggregator. This exception
 * wraps errors such as pipeline misconfigurations, decoding failures, or
 * unexpected exceptions during non-blocking request processing.
 *
 * @see io.maksymuimanov.task.server.HttpServerEndpointChannelInboundHandler
 */
public class HttpServerEndpointChannelInboundHandlingException extends RuntimeException {
    /**
     * Constructs a new HttpServerEndpointChannelInboundHandlingException with the specified cause.
     *
     * @param cause the underlying exception that occurred during inbound channel handling
     */
    public HttpServerEndpointChannelInboundHandlingException(Throwable cause) {
        super(cause);
    }
}