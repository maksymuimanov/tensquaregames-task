package io.maksymuimanov.task.server;

import io.maksymuimanov.task.exception.NettyServerException;
import io.maksymuimanov.task.util.ConfigUtils;
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
 * Ensures a graceful startup and shutdown, with structured logging for observability.
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
    /** System property key defining the hostname or IP address on which the Netty server listens. */
    public static final String SERVER_HOST_PROPERTY = "server.host";
    /** System property key defining the TCP port used by the Netty server. */
    public static final String SERVER_PORT_PROPERTY = "server.port";
    /** System property key defining the server socket backlog size for pending incoming connections. */
    public static final String SERVER_SO_BACKLOG_PROPERTY = "server.backlog";
    /** System property key defining whether TCP keep-alive is enabled for server connections. */
    public static final String SERVER_SO_KEEP_ALIVE_PROPERTY = "server.keep-alive";
    /** Default hostname used by the server. */
    public static final String DEFAULT_SERVER_HOST = ConfigUtils.getOrDefault(SERVER_HOST_PROPERTY, "localhost");
    /** Default TCP port for the HTTP server. */
    public static final int DEFAULT_SERVER_PORT = ConfigUtils.getOrDefault(SERVER_PORT_PROPERTY, 8080);
    /** Default maximum number of queued connection requests. */
    public static final int DEFAULT_SO_BACKLOG_VALUE = ConfigUtils.getOrDefault(SERVER_SO_BACKLOG_PROPERTY, 128);
    /** Default socket option for keeping connections alive. */
    public static final boolean DEFAULT_SO_KEEP_ALIVE_VALUE = ConfigUtils.getOrDefault(SERVER_SO_KEEP_ALIVE_PROPERTY, true);
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final ChannelInitializer<SocketChannel> socketChannelInitializer;
    private final String host;
    private final int port;

    /**
     * Creates a server instance with default event loop groups and port.
     *
     * @param socketChannelInitializer the channel initializer configuring HTTP handlers
     */
    public SimpleNettyServer(ChannelInitializer<SocketChannel> socketChannelInitializer) {
        this(DEFAULT_BOSS_GROUP, DEFAULT_WORKER_GROUP, socketChannelInitializer, DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT);
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
            ChannelFuture channelFuture = serverBootstrap.bind(host, port)
                    .sync();
            log.info("Netty server bound on [host={}; port={}], server is ready", host, port);
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
