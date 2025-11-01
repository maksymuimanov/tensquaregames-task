package io.maksymuimanov.task.endpoint;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.jspecify.annotations.NonNull;

public interface HttpResponseSender {
    void send(@NonNull ChannelHandlerContext context, @NonNull Object response, @NonNull HttpResponseStatus status, boolean keepAlive);
}
