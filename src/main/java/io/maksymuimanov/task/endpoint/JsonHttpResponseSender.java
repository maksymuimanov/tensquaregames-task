package io.maksymuimanov.task.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.maksymuimanov.task.exception.HttpResponseSendingException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JsonHttpResponseSender implements HttpResponseSender {
    private final ObjectMapper objectMapper;

    @Override
    public void send(ChannelHandlerContext context, Object response, HttpResponseStatus status, boolean keepAlive) {
        try {
            byte[] jsonBuffer = objectMapper.writeValueAsBytes(response);
            ByteBuf responseBuffer = Unpooled.wrappedBuffer(jsonBuffer);
            int contentLength = responseBuffer.readableBytes();
            HttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, responseBuffer);
            httpResponse.headers()
                    .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                    .setInt(HttpHeaderNames.CONTENT_LENGTH, contentLength)
                    .set(HttpHeaderNames.CONNECTION, keepAlive
                            ? HttpHeaderValues.KEEP_ALIVE
                            : HttpHeaderValues.CLOSE);
            log.info("Sending HTTP response: status={}, keepAlive={}, contentLength={} bytes", status.code(), keepAlive, contentLength);
            ChannelFuture channelFuture = context.writeAndFlush(httpResponse);
            if (!keepAlive) {
                channelFuture.addListener(ChannelFutureListener.CLOSE);
            }
        } catch (Exception e) {
            log.error("Failed to send HTTP response", e);
            throw new HttpResponseSendingException(e);
        }
    }
}
