package io.maksymuimanov.task.server;

import io.maksymuimanov.task.exception.NettyServerException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class SimpleNettyServer implements NettyServer {
    public static final int DEFAULT_SERVER_PORT = 8080;
    public static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(10);
    public static final Duration DEFAULT_WRITE_TIMEOUT = Duration.ofSeconds(10);
    public static final short MAXIMUM_CONTENT_LENGTH = Short.MAX_VALUE;
    public static final int DEFAULT_SO_BACKLOG_VALUE = 128;
    public static final boolean DEFAULT_SO_KEEP_ALIVE_VALUE = true;
    private final int port;
    private final Duration readTimeout;
    private final Duration writeTimeout;
    private final SimpleChannelInboundHandler<FullHttpRequest> serverEndpointHandler;

    public SimpleNettyServer(SimpleChannelInboundHandler<FullHttpRequest> endpointHandlers) {
        this(DEFAULT_SERVER_PORT, DEFAULT_READ_TIMEOUT, DEFAULT_WRITE_TIMEOUT, endpointHandlers);
    }

    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            this.start(bossGroup, workerGroup);
        } catch (Exception e) {
            throw new NettyServerException(e);
        } finally {
            this.stop(bossGroup, workerGroup);
        }
    }

    private void start(EventLoopGroup bossGroup, EventLoopGroup workerGroup) throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline()
                                .addLast(
                                        new HttpServerCodec(),
                                        new HttpObjectAggregator(MAXIMUM_CONTENT_LENGTH),
                                        new ReadTimeoutHandler(readTimeout.toMillis(), TimeUnit.MILLISECONDS),
                                        new WriteTimeoutHandler(writeTimeout.toMillis(), TimeUnit.MILLISECONDS),
                                        serverEndpointHandler
                                );
                    }
                })
                .option(ChannelOption.SO_BACKLOG, DEFAULT_SO_BACKLOG_VALUE)
                .childOption(ChannelOption.SO_KEEPALIVE, DEFAULT_SO_KEEP_ALIVE_VALUE);
        ChannelFuture channelFuture = serverBootstrap.bind(port)
                .sync();
        channelFuture.channel()
                .closeFuture()
                .sync();
    }

    private void stop(EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }
}
