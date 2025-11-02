package io.maksymuimanov.task.endpoint;

import io.maksymuimanov.task.dto.HttpEndpoint;
import io.netty.channel.ChannelHandlerContext;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

/**
 * Defines a contract for processing HTTP endpoints asynchronously within the Netty-based server.
 * <p>
 * Implementations handle non-blocking request execution, response serialization,
 * and optional caching or aggregation logic using {@link CompletableFuture}.
 * Each processor corresponds to a specific {@link HttpEndpoint}.
 *
 * @see HttpEndpoint
 * @see DashboardGetAsyncHttpEndpointProcessor
 * @see HttpResponseSender
 */
public interface AsyncHttpEndpointProcessor {
    /**
     * Returns the descriptor of the HTTP endpoint handled by this processor.
     * <p>
     * The endpoint defines the HTTP method and URI path used for routing requests.
     *
     * @return the {@link HttpEndpoint} associated with this processor
     */
    @NonNull
    HttpEndpoint getEndpoint();

    /**
     * Handles the incoming HTTP request asynchronously.
     * <p>
     * Implementations should process business logic, prepare a response,
     * and use the provided {@link HttpResponseSender} to write it back to the client.
     * This method must not block and should rely on asynchronous execution.
     *
     * @param context Netty channel context for sending the response
     * @param responseSender component responsible for serializing and sending HTTP responses
     * @param keepAlive whether to keep the TCP connection open after sending the response
     * @return a {@link CompletableFuture} that completes when the response has been sent
     */
    @NonNull
    CompletableFuture<Void> process(@NonNull ChannelHandlerContext context, @NonNull HttpResponseSender responseSender, boolean keepAlive);
}