package io.maksymuimanov.task.endpoint;

import io.maksymuimanov.task.dto.HttpEndpoint;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.CompletableFuture;

public interface AsyncHttpEndpointProcessor {
    HttpEndpoint getEndpoint();

    CompletableFuture<Void> process(ChannelHandlerContext context, HttpResponseSender responseSender, boolean keepAlive);
}
