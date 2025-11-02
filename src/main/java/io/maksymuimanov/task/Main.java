package io.maksymuimanov.task;

import io.maksymuimanov.task.application.NettyApplication;
import io.maksymuimanov.task.application.SimpleNettyApplication;
import lombok.extern.slf4j.Slf4j;

/**
 * Application entry point that starts the asynchronous Netty-based
 * API aggregation server. Initializes and runs {@link SimpleNettyApplication},
 * which configures the HTTP server, caching, and API aggregation components.
 *
 * <p>This class simply delegates startup to the application layer
 * and serves as the executable entry for deployment or testing.
 *
 * @see NettyApplication
 */
@Slf4j
public class Main {
    /**
     * Launches the asynchronous Netty application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        NettyApplication application = new SimpleNettyApplication();
        application.run();
    }
}