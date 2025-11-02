package io.maksymuimanov.task.endpoint;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.jspecify.annotations.NonNull;

/**
 * Defines the contract for sending HTTP responses asynchronously
 * through a Netty channel within the Concurrent API Aggregator Service.
 * <p>
 * Implementations handle serialization, header configuration, and
 * connection management (keep-alive or close) for outgoing responses.
 *
 * @see JsonHttpResponseSender
 */
public interface HttpResponseSender {
    /**
     * Sends an HTTP response asynchronously to the client.
     * <p>
     * Implementations typically serialize the {@code response} object,
     * configure standard HTTP headers, and write the payload through
     * the given {@link ChannelHandlerContext}.
     *
     * @param context Netty context used to send the response.
     * @param response Object representing the HTTP response body.
     * @param status HTTP status code (e.g., 200 OK, 404 Not Found).
     * @param keepAlive Whether to maintain the connection after sending.
     */
    void send(@NonNull ChannelHandlerContext context, @NonNull Object response, @NonNull HttpResponseStatus status, boolean keepAlive);
}
