package io.maksymuimanov.task.endpoint;

import io.maksymuimanov.task.dto.ErrorResponse;
import io.maksymuimanov.task.dto.HttpEndpoint;
import io.maksymuimanov.task.exception.HttpEndpointDirectingException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class SimpleHttpEndpointDirector implements HttpEndpointDirector {
    private static final ErrorResponse NOT_FOUND_MESSAGE = new ErrorResponse("Not Found");
    private final Map<HttpEndpoint, AsyncHttpEndpointProcessor> endpointHandlers;

    @Override
    public void direct(ChannelHandlerContext context, FullHttpRequest request, HttpResponseSender responseSender) {
        try {
            boolean keepAlive = HttpUtil.isKeepAlive(request);
            String path = new QueryStringDecoder(request.uri()).path();
            HttpMethod httpMethod = request.method();
            HttpEndpoint httpEndpoint = new HttpEndpoint(path, httpMethod);
            if (endpointHandlers.containsKey(httpEndpoint)) {
                AsyncHttpEndpointProcessor endpointHandler = endpointHandlers.get(httpEndpoint);
                endpointHandler.process(context, responseSender, keepAlive)
                        .whenComplete((v, ex) -> {
                            if (ex != null) {
                                log.error("Endpoint failed", ex);
                                if (context.channel().isActive()) {
                                    responseSender.send(context, Map.of("error", "Unexpected server error"), HttpResponseStatus.INTERNAL_SERVER_ERROR, false);
                                }
                            }
                        });
            } else {
                responseSender.send(context, NOT_FOUND_MESSAGE, HttpResponseStatus.NOT_FOUND, keepAlive);
            }
        } catch (Exception e) {
            throw new HttpEndpointDirectingException(e);
        }
    }
}
