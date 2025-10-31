package io.maksymuimanov.task.endpoint;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

public interface HttpResponseSender {
    void send(ChannelHandlerContext context, FullHttpRequest request, HttpResponseStatus status, Object response);
}
