package io.maksymuimanov.task.endpoint;

import io.maksymuimanov.task.dto.ErrorResponse;
import io.maksymuimanov.task.dto.HttpEndpoint;
import io.maksymuimanov.task.exception.HttpEndpointDirectingException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class SimpleHttpEndpointDirector implements HttpEndpointDirector {
    public static final ErrorResponse UNEXPECTED_SERVER_ERROR_MESSAGE = new ErrorResponse("Unexpected server error");
    public static final ErrorResponse NOT_FOUND_MESSAGE = new ErrorResponse("Not Found");
    @NonNull
    private final Map<HttpEndpoint, AsyncHttpEndpointProcessor> endpointProcessors;

    @Override
    public void direct(@NonNull ChannelHandlerContext context, @NonNull FullHttpRequest request, @NonNull HttpResponseSender responseSender) {
        try {
            boolean keepAlive = HttpUtil.isKeepAlive(request);
            String path = new QueryStringDecoder(request.uri()).path();
            HttpMethod httpMethod = request.method();
            HttpEndpoint httpEndpoint = new HttpEndpoint(path, httpMethod);
            if (endpointProcessors.containsKey(httpEndpoint)) {
                log.info("Routing to endpoint processor: method={}, path={}, keepAlive={}", httpMethod, path, keepAlive);
                AsyncHttpEndpointProcessor endpointHandler = endpointProcessors.get(httpEndpoint);
                endpointHandler.process(context, responseSender, keepAlive)
                        .whenComplete((v, ex) -> {
                            if (ex == null) {
                                log.info("Endpoint processing completed: method={}, path={}", httpMethod, path);
                            } else {
                                log.error("Endpoint processing failed: method={}, path={}", httpMethod, path, ex);
                                if (context.channel().isActive()) {
                                    responseSender.send(context, UNEXPECTED_SERVER_ERROR_MESSAGE, HttpResponseStatus.INTERNAL_SERVER_ERROR, keepAlive);
                                }
                            }
                        });
            } else {
                log.warn("No endpoint matched: method={}, path={}, responding 404", httpMethod, path);
                responseSender.send(context, NOT_FOUND_MESSAGE, HttpResponseStatus.NOT_FOUND, keepAlive);
            }
        } catch (Exception e) {
            log.error("Failed to direct request", e);
            throw new HttpEndpointDirectingException(e);
        }
    }
}
