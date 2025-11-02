package io.maksymuimanov.task.endpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.maksymuimanov.task.exception.HttpResponseSendingException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JsonHttpResponseSenderTests {
    static final Object TEST_RESPONSE_BODY = "{\"a\": \"a\"}";
    static final int TEST_JSON_BUFFER_SIZE = 1;
    static final byte[] TEST_JSON_BUFFER = new byte[TEST_JSON_BUFFER_SIZE];
    static final ByteBuf TEST_RESPONSE_BUFFER = Unpooled.wrappedBuffer(TEST_JSON_BUFFER);
    HttpResponseSender jsonHttpResponseSender;
    ObjectMapper objectMapper;
    ChannelHandlerContext context;
    HttpResponseStatus status;
    HttpResponse httpResponse;
    ChannelFuture channelFuture;

    @BeforeEach
    void setUp() {
        objectMapper = Mockito.mock(ObjectMapper.class);
        context = Mockito.mock(ChannelHandlerContext.class);
        status = HttpResponseStatus.OK;
        httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, TEST_RESPONSE_BUFFER);
        httpResponse.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                .setInt(HttpHeaderNames.CONTENT_LENGTH, TEST_JSON_BUFFER_SIZE);
        channelFuture = Mockito.mock(ChannelFuture.class);
        jsonHttpResponseSender = new JsonHttpResponseSender(objectMapper);
    }

    @Test
    void shouldSendKeepAliveSuccessfully() throws JsonProcessingException {
        boolean keepAlive = true;
        httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

        Mockito.when(objectMapper.writeValueAsBytes(TEST_RESPONSE_BODY)).thenReturn(TEST_JSON_BUFFER);
        Mockito.when(context.writeAndFlush(httpResponse)).thenReturn(channelFuture);

        jsonHttpResponseSender.send(context, TEST_RESPONSE_BODY, status, keepAlive);
        Mockito.verify(context).writeAndFlush(httpResponse);
        Mockito.verify(channelFuture, Mockito.never()).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    void shouldSendNonKeepAliveSuccessfully() throws JsonProcessingException {
        boolean keepAlive = false;
        httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);

        Mockito.when(objectMapper.writeValueAsBytes(TEST_RESPONSE_BODY)).thenReturn(TEST_JSON_BUFFER);
        Mockito.when(context.writeAndFlush(httpResponse)).thenReturn(channelFuture);

        jsonHttpResponseSender.send(context, TEST_RESPONSE_BODY, status, keepAlive);
        Mockito.verify(context).writeAndFlush(httpResponse);
        Mockito.verify(channelFuture).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    void shouldFailToSend() throws JsonProcessingException {
        Mockito.when(objectMapper.writeValueAsBytes(TEST_RESPONSE_BODY)).thenThrow(RuntimeException.class);

        Assertions.assertThrows(HttpResponseSendingException.class, () -> jsonHttpResponseSender.send(context, TEST_RESPONSE_BODY, status, false));
    }
}
