package io.maksymuimanov.task.endpoint;

import io.maksymuimanov.task.dto.HttpEndpoint;
import io.maksymuimanov.task.exception.HttpEndpointDirectingException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class SimpleHttpEndpointDirector implements HttpEndpointDirector {
    private static final String NOT_FOUND_MESSAGE = "Not Found";
    private final Map<HttpEndpoint, AsyncHttpEndpointProcessor> endpointHandlers;

    @Override
    public void direct(ChannelHandlerContext context, FullHttpRequest request, HttpResponseSender responseSender) {
        try {
            HttpEndpoint httpEndpoint = new HttpEndpoint(request.uri(), request.method());
            if (endpointHandlers.containsKey(httpEndpoint)) {
                AsyncHttpEndpointProcessor endpointHandler = endpointHandlers.get(httpEndpoint);
                endpointHandler.process(request, responseSender, context);
            } else {
                responseSender.send(context, request, HttpResponseStatus.NOT_FOUND, NOT_FOUND_MESSAGE);
            }
        } catch (Exception e) {
            throw new HttpEndpointDirectingException(e);
        }
    }
}
