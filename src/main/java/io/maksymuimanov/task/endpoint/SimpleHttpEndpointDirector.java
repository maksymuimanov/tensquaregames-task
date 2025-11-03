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

/**
 * Routes incoming HTTP requests to their appropriate asynchronous endpoint processors.
 * <p>
 * This class acts as the central dispatcher in the Netty server pipeline,
 * matching each request’s {@link io.netty.handler.codec.http.HttpMethod} and URI path
 * against registered {@link AsyncHttpEndpointProcessor}s. It executes the matched
 * processor asynchronously and ensures proper response handling and error recovery.
 *
 * @see HttpEndpointDirector
 * @see AsyncHttpEndpointProcessor
 * @see HttpResponseSender
 * @see HttpEndpoint
 */
@Slf4j
@RequiredArgsConstructor
public class SimpleHttpEndpointDirector implements HttpEndpointDirector {
    /** Generic error message used when an unexpected server-side failure occurs. */
    public static final ErrorResponse UNEXPECTED_SERVER_ERROR_MESSAGE = new ErrorResponse("Unexpected server error");
    /** Error message returned when no matching endpoint is found. */
    public static final ErrorResponse NOT_FOUND_MESSAGE = new ErrorResponse("Not Found");
    @NonNull
    private final Map<HttpEndpoint, AsyncHttpEndpointProcessor> endpointProcessors;

    /**
     * Directs an incoming HTTP request to the appropriate {@link AsyncHttpEndpointProcessor}.
     * <p>
     * Resolves the request path and method, finds the matching endpoint, and delegates
     * asynchronous execution. If no endpoint matches, a 404 response is sent.
     * Errors during processing trigger a 500 Internal Server Error response.
     *
     * @param context Netty channel context used to send responses
     * @param request the full HTTP request received by the server
     * @param responseSender helper for serializing and sending JSON responses
     * @throws HttpEndpointDirectingException if routing or endpoint execution fails unexpectedly
     */
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
                            if (ex == null || !context.channel().isActive()) {
                                log.info("Endpoint processing completed: method={}, path={}", httpMethod, path);
                            } else {
                                log.error("Endpoint processing failed: method={}, path={}", httpMethod, path, ex);
                                responseSender.send(context, UNEXPECTED_SERVER_ERROR_MESSAGE, HttpResponseStatus.INTERNAL_SERVER_ERROR, keepAlive);
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
