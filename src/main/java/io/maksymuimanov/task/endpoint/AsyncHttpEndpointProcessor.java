package io.maksymuimanov.task.endpoint;

import io.maksymuimanov.task.dto.HttpEndpoint;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.concurrent.CompletableFuture;

public interface AsyncHttpEndpointProcessor {
    HttpEndpoint getEndpoint();

    CompletableFuture<Void> process(FullHttpRequest request, HttpResponseSender responseSender, ChannelHandlerContext context);
}
