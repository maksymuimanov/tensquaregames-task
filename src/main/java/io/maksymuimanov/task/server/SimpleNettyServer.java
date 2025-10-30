package io.maksymuimanov.task.server;

import io.maksymuimanov.task.endpoint.DashboardServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public class SimpleNettyServer implements NettyServer {
    public static final int SERVER_PORT = 8080;
    public static final int MAXIMUM_CONTENT_LENGTH = Short.MAX_VALUE;

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
                                        new HttpObjectAggregator(MAXIMUM_CONTENT_LENGTH),
                                        new DashboardServerHandler()
                                );
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        ChannelFuture channelFuture = serverBootstrap.bind(SERVER_PORT)
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
