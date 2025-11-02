package io.maksymuimanov.task.server;

import io.maksymuimanov.task.exception.HttpSocketChannelInitializingException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configures the Netty {@link SocketChannel} pipeline for handling incoming HTTP requests
 * in a non-blocking, asynchronous environment.
 * <p>
 * Adds HTTP codec, content aggregation, read/write timeout handlers, and the main
 * server endpoint handler that processes {@link FullHttpRequest} messages.
 * Used during server startup to prepare channels for concurrent HTTP traffic.
 *
 * @see ChannelInitializer
 * @see SimpleChannelInboundHandler
 */
@Slf4j
@RequiredArgsConstructor
public class HttpSocketChannelInitializer extends ChannelInitializer<SocketChannel> {
    /** Maximum size of the aggregated HTTP content in bytes. */
    public static final short DEFAULT_MAXIMUM_CONTENT_LENGTH = Short.MAX_VALUE;
    /** Default read timeout duration for incoming requests. */
    public static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(10);
    /** Default write timeout duration for outgoing responses. */
    public static final Duration DEFAULT_WRITE_TIMEOUT = Duration.ofSeconds(10);
    private final int maximumContentLength;
    private final Duration readTimeout;
    private final Duration writeTimeout;
    private final SimpleChannelInboundHandler<FullHttpRequest> serverEndpointHandler;

    /**
     * Creates an initializer with default configuration values.
     *
     * @param serverEndpointHandler the main request handler for processing HTTP messages
     */
    public HttpSocketChannelInitializer(SimpleChannelInboundHandler<FullHttpRequest> serverEndpointHandler) {
        this(DEFAULT_MAXIMUM_CONTENT_LENGTH, DEFAULT_READ_TIMEOUT, DEFAULT_WRITE_TIMEOUT, serverEndpointHandler);
    }

    /**
     * Initializes the Netty channel pipeline with HTTP codec, timeout, and request handler components.
     * Executed automatically by Netty when a new {@link SocketChannel} is created.
     *
     * @param socketChannel the channel being initialized
     * @throws HttpSocketChannelInitializingException if any handler setup fails
     */
    @Override
    protected void initChannel(SocketChannel socketChannel) {
        try {
            log.info("Initializing socket channel...");
            socketChannel.pipeline()
                    .addLast(
                            new HttpServerCodec(),
                            new HttpObjectAggregator(maximumContentLength),
                            new ReadTimeoutHandler(readTimeout.toMillis(), TimeUnit.MILLISECONDS),
                            new WriteTimeoutHandler(writeTimeout.toMillis(), TimeUnit.MILLISECONDS),
                            serverEndpointHandler
                    );
            log.info("Socket channel is successfully initialized");
        } catch (Exception e) {
            log.error("Failed to initialize HTTP channel for: {}", e.getMessage(), e);
            throw new HttpSocketChannelInitializingException(e);
        }
    }
}