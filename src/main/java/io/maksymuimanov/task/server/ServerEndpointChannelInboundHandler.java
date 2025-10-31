package io.maksymuimanov.task.server;

import io.maksymuimanov.task.dto.ErrorResponse;
import io.maksymuimanov.task.endpoint.HttpEndpointDirector;
import io.maksymuimanov.task.endpoint.HttpResponseSender;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class ServerEndpointChannelInboundHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    public static final ErrorResponse INTERNAL_SERVER_ERROR_MESSAGE = new ErrorResponse("Internal server error");
    private final HttpResponseSender responseSender;
    private final HttpEndpointDirector endpointDirector;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
        endpointDirector.direct(ctx, msg, responseSender);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Unhandled exception", cause);
        responseSender.send(ctx, INTERNAL_SERVER_ERROR_MESSAGE, HttpResponseStatus.INTERNAL_SERVER_ERROR, false);
    }
}
