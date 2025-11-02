package io.maksymuimanov.task.server;

import io.maksymuimanov.task.exception.NettyServerException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A lightweight asynchronous HTTP server built on top of Netty.
 * <p>
 * Initializes and manages Netty event loops, binds the server to a configured port,
 * and handles incoming HTTP traffic using the provided {@link ChannelInitializer}.
 * Ensures graceful startup and shutdown, with structured logging for observability.
 *
 * @see NettyServer
 * @see EventLoopGroup
 * @see ChannelInitializer
 */
@Slf4j
@RequiredArgsConstructor
public class SimpleNettyServer implements NettyServer {
    /** Default boss thread group handling connection accepts events. */
    public static final EventLoopGroup DEFAULT_BOSS_GROUP = new NioEventLoopGroup();
    /** Default worker thread group handling read/write I/O events. */
    public static final EventLoopGroup DEFAULT_WORKER_GROUP = new NioEventLoopGroup();
    /** Default TCP port for the HTTP server. */
    public static final int DEFAULT_SERVER_PORT = 8080;
    /** Default maximum number of queued connection requests. */
    public static final int DEFAULT_SO_BACKLOG_VALUE = 128;
    /** Default socket option for keeping connections alive. */
    public static final boolean DEFAULT_SO_KEEP_ALIVE_VALUE = true;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final ChannelInitializer<SocketChannel> socketChannelInitializer;
    private final int port;

    /**
     * Creates a server instance with default event loop groups and port.
     *
     * @param socketChannelInitializer the channel initializer configuring HTTP handlers
     */
    public SimpleNettyServer(ChannelInitializer<SocketChannel> socketChannelInitializer) {
        this(socketChannelInitializer, DEFAULT_BOSS_GROUP, DEFAULT_WORKER_GROUP);
    }

    /**
     * Creates a server instance with custom event loops but default port.
     *
     * @param bossGroup boss group handling connection accepts
     * @param workerGroup worker group handling network I/O
     * @param socketChannelInitializer initializer configuring HTTP pipeline
     */
    public SimpleNettyServer(ChannelInitializer<SocketChannel> socketChannelInitializer, EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        this(bossGroup, workerGroup, socketChannelInitializer, DEFAULT_SERVER_PORT);
    }

    /**
     * Starts the Netty server lifecycle.
     * Invokes asynchronous channel binding and blocks until shutdown.
     *
     * @throws NettyServerException if server startup fails
     */
    @Override
    public void run() {
        try {
            log.info("Starting Netty HTTP server on port={}", port);
            this.start(bossGroup, workerGroup);
        } catch (Exception e) {
            log.error("Netty server failed to start", e);
            throw new NettyServerException(e);
        } finally {
            this.stop(bossGroup, workerGroup);
        }
    }

    /**
     * Configures and starts the Netty server bootstrap.
     * Sets channel options, binds to the port, and blocks until the server is closed.
     *
     * @param bossGroup event loop handling new connections
     * @param workerGroup event loop handling active connections
     * @throws NettyServerException if the server fails to bind or initialize
     */
    private void start(EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(socketChannelInitializer)
                    .option(ChannelOption.SO_BACKLOG, DEFAULT_SO_BACKLOG_VALUE)
                    .childOption(ChannelOption.SO_KEEPALIVE, DEFAULT_SO_KEEP_ALIVE_VALUE);
            ChannelFuture channelFuture = serverBootstrap.bind(port)
                    .sync();
            log.info("Netty server bound on port={}, awaiting close", port);
            channelFuture.channel()
                    .closeFuture()
                    .sync();
            log.info("Netty server channel closed");
        } catch (Exception e) {
            throw new NettyServerException(e);
        }
    }

    /**
     * Gracefully shuts down Netty event loop groups.
     * Called automatically on server termination.
     *
     * @param bossGroup event loop handling incoming connections
     * @param workerGroup event loop handling I/O operations
     * @throws NettyServerException if shutdown fails
     */
    private void stop(EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        try {
            log.info("Shutting down Netty event loops");
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            log.info("Netty event loops shutdown initiated");
        } catch (Exception e) {
            throw new NettyServerException(e);
        }
    }
}
