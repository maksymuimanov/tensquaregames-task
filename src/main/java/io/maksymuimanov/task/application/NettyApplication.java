package io.maksymuimanov.task.application;

/**
 * Represents the main entry point for launching a Netty-based asynchronous application.
 * <p>
 * Implementations are responsible for initializing application components,
 * configuring network handlers, and starting the non-blocking Netty HTTP server.
 * Designed for high-performance concurrent workloads with asynchronous I/O.
 *
 * @see SimpleNettyApplication
 * @see io.maksymuimanov.task.Main
 */
public interface NettyApplication {
    /**
     * Starts the Netty-based application lifecycle.
     * Typically, initializes all dependencies, configures the server,
     * and begins handling incoming requests asynchronously.
     */
    void run();
}