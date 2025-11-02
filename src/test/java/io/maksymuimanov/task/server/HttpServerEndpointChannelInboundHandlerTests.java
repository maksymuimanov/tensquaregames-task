package io.maksymuimanov.task.server;

import io.maksymuimanov.task.endpoint.HttpEndpointDirector;
import io.maksymuimanov.task.endpoint.HttpResponseSender;
import io.maksymuimanov.task.exception.HttpServerEndpointChannelInboundHandlingException;
import io.maksymuimanov.task.util.ReflectionUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class HttpServerEndpointChannelInboundHandlerTests {
    public static final String CHANNEL_READ_0_METHOD_NAME = "channelRead0";
    SimpleChannelInboundHandler<FullHttpRequest> httpServerEndpointChannelInboundHandler;
    HttpResponseSender responseSender;
    HttpEndpointDirector endpointDirector;
    ChannelHandlerContext ctx;
    FullHttpRequest msg;
    Throwable cause;

    @BeforeEach
    void setUp() {
        responseSender = Mockito.mock(HttpResponseSender.class);
        endpointDirector = Mockito.mock(HttpEndpointDirector.class);
        ctx = Mockito.mock(ChannelHandlerContext.class);
        msg = Mockito.mock(FullHttpRequest.class);
        cause = Mockito.mock(Throwable.class);
        httpServerEndpointChannelInboundHandler = new HttpServerEndpointChannelInboundHandler(responseSender, endpointDirector);
    }

    @Test
    void shouldChannelReadSuccessfully() {
        Assertions.assertDoesNotThrow(() -> ReflectionUtils.callMethod(httpServerEndpointChannelInboundHandler, CHANNEL_READ_0_METHOD_NAME, new Class<?>[]{ChannelHandlerContext.class, FullHttpRequest.class}, new Object[]{ctx, msg}));
        Mockito.verify(endpointDirector, Mockito.times(1)).direct(ctx, msg, responseSender);
    }

    @Test
    void shouldFailToChannelRead() {
        Mockito.doThrow(RuntimeException.class).when(endpointDirector).direct(ctx, msg, responseSender);

        Assertions.assertThrows(HttpServerEndpointChannelInboundHandlingException.class, () -> ReflectionUtils.callMethod(httpServerEndpointChannelInboundHandler, CHANNEL_READ_0_METHOD_NAME, new Class<?>[]{ChannelHandlerContext.class, FullHttpRequest.class}, new Object[]{ctx, msg}));
        Mockito.verify(endpointDirector, Mockito.times(1)).direct(ctx, msg, responseSender);
    }

    @Test
    void shouldExceptionCaughtSuccessfully() {
        Assertions.assertDoesNotThrow(() -> httpServerEndpointChannelInboundHandler.exceptionCaught(ctx, cause));
        Mockito.verify(responseSender, Mockito.times(1)).send(ctx, HttpServerEndpointChannelInboundHandler.INTERNAL_SERVER_ERROR_MESSAGE, HttpResponseStatus.INTERNAL_SERVER_ERROR, true);
    }

    @Test
    void shouldFailToExceptionCaught() {
        Mockito.doThrow(RuntimeException.class).when(responseSender).send(ctx, HttpServerEndpointChannelInboundHandler.INTERNAL_SERVER_ERROR_MESSAGE, HttpResponseStatus.INTERNAL_SERVER_ERROR, true);

        Assertions.assertThrows(HttpServerEndpointChannelInboundHandlingException.class, () -> httpServerEndpointChannelInboundHandler.exceptionCaught(ctx, cause));
        Mockito.verify(responseSender, Mockito.times(1)).send(ctx, HttpServerEndpointChannelInboundHandler.INTERNAL_SERVER_ERROR_MESSAGE, HttpResponseStatus.INTERNAL_SERVER_ERROR, true);
    }
}
