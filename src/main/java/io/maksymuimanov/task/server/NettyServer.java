package io.maksymuimanov.task.server;

/**
 * Represents a generic Netty-based server capable of handling asynchronous network I/O.
 * <p>
 * Implementations define how the server initializes, binds to a port, and manages
 * the Netty event loop lifecycle. Designed for non-blocking, high-performance HTTP processing.
 *
 * @see SimpleNettyServer
 */
public interface NettyServer {
    /**
     * Starts the Netty server and blocks until shutdown.
     * Implementations are responsible for graceful startup and cleanup.
     */
    void run();
}
