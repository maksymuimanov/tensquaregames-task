package io.maksymuimanov.task.endpoint;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.jspecify.annotations.NonNull;

public interface HttpEndpointDirector {
    void direct(@NonNull ChannelHandlerContext context, @NonNull FullHttpRequest request, @NonNull HttpResponseSender responseSender);
}
