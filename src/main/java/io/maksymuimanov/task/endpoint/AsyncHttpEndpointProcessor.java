package io.maksymuimanov.task.endpoint;

import io.maksymuimanov.task.dto.HttpEndpoint;
import io.netty.channel.ChannelHandlerContext;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public interface AsyncHttpEndpointProcessor {
    @NonNull
    HttpEndpoint getEndpoint();

    @NonNull
    CompletableFuture<Void> process(@NonNull ChannelHandlerContext context, @NonNull HttpResponseSender responseSender, boolean keepAlive);
}
