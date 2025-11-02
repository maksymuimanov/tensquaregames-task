package server;

import io.maksymuimanov.task.exception.HttpSocketChannelInitializingException;
import io.maksymuimanov.task.server.HttpSocketChannelInitializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import util.ReflectionUtils;

@SuppressWarnings("unchecked")
class HttpSocketChannelInitializerTests {
    public static final String INIT_CHANNEL_METHOD_NAME = "initChannel";
    ChannelInitializer<SocketChannel> httpSocketChannelInitializer;
    SimpleChannelInboundHandler<FullHttpRequest> serverEndpointHandler;
    SocketChannel socketChannel;
    ChannelPipeline pipeline;

    @BeforeEach
    void setUp() {
        serverEndpointHandler = Mockito.mock(SimpleChannelInboundHandler.class);
        socketChannel = Mockito.mock(SocketChannel.class);
        pipeline = Mockito.mock(ChannelPipeline.class);
        httpSocketChannelInitializer = new HttpSocketChannelInitializer(serverEndpointHandler);
    }

    @Test
    void shouldInitChannelSuccessfully() {
        Mockito.when(socketChannel.pipeline()).thenReturn(pipeline);

        Assertions.assertDoesNotThrow(() -> ReflectionUtils.callMethod(httpSocketChannelInitializer, INIT_CHANNEL_METHOD_NAME, new Class<?>[]{SocketChannel.class}, new Object[]{socketChannel}));
    }

    @Test
    void shouldFailToInitChannel() {
        Mockito.when(socketChannel.pipeline()).thenThrow(RuntimeException.class);

        Assertions.assertThrows(HttpSocketChannelInitializingException.class, () -> ReflectionUtils.callMethod(httpSocketChannelInitializer, INIT_CHANNEL_METHOD_NAME, new Class<?>[]{SocketChannel.class}, new Object[]{socketChannel}));
    }
}