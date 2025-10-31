package io.maksymuimanov.task.endpoint;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public interface HttpEndpointDirector {
    void direct(ChannelHandlerContext context, FullHttpRequest request, HttpResponseSender responseSender);
}
