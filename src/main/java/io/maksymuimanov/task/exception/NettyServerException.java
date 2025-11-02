package io.maksymuimanov.task.exception;

/**
 * Represents a general failure within the Netty HTTP server used by the
 * asynchronous API aggregator. This exception wraps critical errors such as
 * server startup failures, channel binding issues, or unhandled runtime exceptions
 * in the Netty event loop.
 *
 * @see io.maksymuimanov.task.server.NettyServer
 * @see io.maksymuimanov.task.server.SimpleNettyServer
 */
public class NettyServerException extends RuntimeException {
    /**
     * Constructs a new NettyServerException with the specified cause.
     *
     * @param cause the underlying exception that triggered the Netty server failure
     */
    public NettyServerException(Throwable cause) {
        super(cause);
    }
}