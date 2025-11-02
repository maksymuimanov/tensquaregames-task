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

@Slf4j
@RequiredArgsConstructor
public class HttpSocketChannelInitializer extends ChannelInitializer<SocketChannel> {
    public static final short DEFAULT_MAXIMUM_CONTENT_LENGTH = Short.MAX_VALUE;
    public static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(10);
    public static final Duration DEFAULT_WRITE_TIMEOUT = Duration.ofSeconds(10);
    private final int maximumContentLength;
    private final Duration readTimeout;
    private final Duration writeTimeout;
    private final SimpleChannelInboundHandler<FullHttpRequest> serverEndpointHandler;

    public HttpSocketChannelInitializer(SimpleChannelInboundHandler<FullHttpRequest> serverEndpointHandler) {
        this(DEFAULT_MAXIMUM_CONTENT_LENGTH, DEFAULT_READ_TIMEOUT, DEFAULT_WRITE_TIMEOUT, serverEndpointHandler);
    }

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
