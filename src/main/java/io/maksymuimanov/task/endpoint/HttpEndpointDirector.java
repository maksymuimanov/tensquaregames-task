package io.maksymuimanov.task.endpoint;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.jspecify.annotations.NonNull;

/**
 * Defines a contract for directing incoming HTTP requests to their appropriate handlers.
 * <p>
 * Implementations of this interface are responsible for analyzing each Netty
 * {@link FullHttpRequest}, resolving the corresponding
 * endpoint, and delegating processing to an {@link AsyncHttpEndpointProcessor}.
 * <p>
 * This abstraction allows flexible routing and decouples request handling from
 * low-level Netty networking logic.
 *
 * @see SimpleHttpEndpointDirector
 * @see HttpResponseSender
 */
public interface HttpEndpointDirector {
    /**
     * Routes the given HTTP request to a matching asynchronous endpoint processor.
     * <p>
     * Implementations should determine the request’s path and HTTP method, locate
     * the correct handler, and trigger its asynchronous processing. If no matching
     * endpoint exists, a 404 response should be sent.
     *
     * @param context Netty channel context used to send responses
     * @param request the incoming HTTP request
     * @param responseSender component responsible for sending serialized responses
     */
    void direct(@NonNull ChannelHandlerContext context, @NonNull FullHttpRequest request, @NonNull HttpResponseSender responseSender);
}