package io.maksymuimanov.task.server;

import io.maksymuimanov.task.cache.AsyncCacheManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SimpleNettyServer implements NettyServer {
    public static final int DEFAULT_SERVER_PORT = 8080;
    public static final short MAXIMUM_CONTENT_LENGTH = Short.MAX_VALUE;
    private final int port;
    private final List<SimpleChannelInboundHandler<FullHttpRequest>> endpointHandlers;
    private final AsyncCacheManager cacheManager;

    public SimpleNettyServer(List<SimpleChannelInboundHandler<FullHttpRequest>> endpointHandlers, AsyncCacheManager cacheManager) {
        this(DEFAULT_SERVER_PORT, endpointHandlers, cacheManager);
    }

    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            this.start(bossGroup, workerGroup);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
                                        new HttpObjectAggregator(MAXIMUM_CONTENT_LENGTH)
                                )
                                .addLast(endpointHandlers.toArray(ChannelHandler[]::new));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        ChannelFuture channelFuture = serverBootstrap.bind(port)
                .sync();
        channelFuture.channel()
                .closeFuture()
                .sync();
    }

    private void stop(EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        cacheManager.close();
    }
}
