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

@Slf4j
@RequiredArgsConstructor
public class SimpleNettyServer implements NettyServer {
    public static final EventLoopGroup DEFAULT_BOSS_GROUP = new NioEventLoopGroup();
    public static final EventLoopGroup DEFAULT_WORKER_GROUP = new NioEventLoopGroup();
    public static final int DEFAULT_SERVER_PORT = 8080;
    public static final int DEFAULT_SO_BACKLOG_VALUE = 128;
    public static final boolean DEFAULT_SO_KEEP_ALIVE_VALUE = true;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final ChannelInitializer<SocketChannel> socketChannelInitializer;
    private final int port;

    public SimpleNettyServer(ChannelInitializer<SocketChannel> socketChannelInitializer) {
        this(socketChannelInitializer, DEFAULT_BOSS_GROUP, DEFAULT_WORKER_GROUP);
    }

    public SimpleNettyServer(ChannelInitializer<SocketChannel> socketChannelInitializer, EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        this(bossGroup, workerGroup, socketChannelInitializer, DEFAULT_SERVER_PORT);
    }

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
