package io.maksymuimanov.task.server;

import io.maksymuimanov.task.dto.ErrorResponse;
import io.maksymuimanov.task.endpoint.HttpEndpointDirector;
import io.maksymuimanov.task.endpoint.HttpResponseSender;
import io.maksymuimanov.task.exception.HttpServerEndpointChannelInboundHandlingException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles inbound HTTP requests and delegates them to the appropriate endpoint processor
 * using {@link HttpEndpointDirector}.
 * <p>
 * Acts as the main entry point for all HTTP traffic in the Netty server pipeline.
 * Logs request details, forwards them to the director for async processing, and
 * ensures proper error handling for any unexpected exceptions.
 *
 * @see SimpleChannelInboundHandler
 * @see HttpResponseSender
 * @see HttpEndpointDirector
 */
@Slf4j
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class HttpServerEndpointChannelInboundHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    /** Standard message returned when an internal server error occurs. */
    public static final ErrorResponse INTERNAL_SERVER_ERROR_MESSAGE = new ErrorResponse("Internal server error");
    private final HttpResponseSender responseSender;
    private final HttpEndpointDirector endpointDirector;

    /**
     * Handles a fully decoded HTTP request.
     * Logs the method and URI, then delegates processing to the {@link HttpEndpointDirector}.
     *
     * @param ctx the Netty channel context
     * @param msg the incoming full HTTP request
     * @throws HttpServerEndpointChannelInboundHandlingException if request delegation fails
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
        try {
            log.info("Received request from client: method={}, uri={}", msg.method(), msg.uri());
            endpointDirector.direct(ctx, msg, responseSender);
        } catch (Exception e) {
            throw new HttpServerEndpointChannelInboundHandlingException(e);
        }
    }

    /**
     * Handles any unhandled exceptions thrown during request processing.
     * Logs the error and sends a generic {@code 500 Internal Server Error} response.
     *
     * @param ctx the channel context for writing the error response
     * @param cause the root cause of the exception
     * @throws HttpServerEndpointChannelInboundHandlingException if response sending fails
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        try {
            log.error("Unhandled exception in channel pipeline", cause);
            responseSender.send(ctx, INTERNAL_SERVER_ERROR_MESSAGE, HttpResponseStatus.INTERNAL_SERVER_ERROR, true);
        } catch (Exception e) {
            throw new HttpServerEndpointChannelInboundHandlingException(e);
        }
    }
}
