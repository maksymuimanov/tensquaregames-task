package io.maksymuimanov.task.endpoint;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;

public interface HttpResponseSender {
    void send(ChannelHandlerContext context, Object response, HttpResponseStatus status, boolean keepAlive);
}
