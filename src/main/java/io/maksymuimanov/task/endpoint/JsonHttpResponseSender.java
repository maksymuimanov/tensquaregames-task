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
import org.jspecify.annotations.NonNull;

/**
 * Sends JSON-based HTTP responses asynchronously through a Netty channel.
 * <p>
 * Converts Java objects to JSON using {@link ObjectMapper} and writes the serialized
 * payload to the network channel as a full HTTP/1.1 response. Supports both
 * persistent (keep-alive) and one-shot (close) connections.
 * <p>
 * This component is responsible for finalizing outbound HTTP communication
 * in the Concurrent API Aggregator Service.
 *
 * @see ObjectMapper
 * @see HttpResponseSender
 */
@Slf4j
@RequiredArgsConstructor
public class JsonHttpResponseSender implements HttpResponseSender {
    @NonNull
    private final ObjectMapper objectMapper;

    /**
     * Serializes the given response object into JSON and writes it asynchronously
     * to the provided Netty {@link ChannelHandlerContext}.
     * <p>
     * Automatically sets HTTP headers including {@code Content-Type}, {@code Content-Length},
     * and {@code Connection}. Closes the connection if {@code keepAlive} is {@code false}.
     *
     * @param context Netty channel context used to write the response.
     * @param response Response body object to serialize and send as JSON.
     * @param status HTTP status code to send (e.g., 200 OK, 500 Internal Server Error).
     * @param keepAlive Whether to keep the connection alive after sending the response.
     * @throws HttpResponseSendingException if the response cannot be serialized or sent.
     */
    @Override
    public void send(@NonNull ChannelHandlerContext context, @NonNull Object response, @NonNull HttpResponseStatus status, boolean keepAlive) {
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
