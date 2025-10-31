package io.maksymuimanov.task.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.maksymuimanov.task.api.AsyncApiAggregator;
import io.maksymuimanov.task.cache.AsyncCacheManager;
import io.maksymuimanov.task.dto.DashboardResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class DashboardEndpointHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    public static final String DASHBOARD_ENDPOINT = "/api/dashboard";
    public static final String DASHBOARD_CACHE_KEY = "dashboard";
    private final AsyncApiAggregator<DashboardResponse> apiAggregator;
    private final ObjectMapper objectMapper;
    private final AsyncCacheManager cacheManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
        if (!DASHBOARD_ENDPOINT.equals(msg.uri())) {
            this.sendResponse(ctx, HttpResponseStatus.NOT_FOUND, "Not Found");
            return;
        }

        apiAggregator.aggregate()
                .exceptionallyCompose(ex -> cacheManager.get(DASHBOARD_CACHE_KEY, DashboardResponse.class)
                        .thenApply(optional -> optional.orElse(null)))
                .thenCompose(response -> {
                    if (response != null)
                        return cacheManager.put(DASHBOARD_CACHE_KEY, response)
                                .thenApply(v -> response);
                    return CompletableFuture.completedFuture(null);
                })
                .thenAccept(response -> {
                    if (response != null) {
                        sendResponse(ctx, HttpResponseStatus.OK, response);
                    } else {
                        sendResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Failed to fetch data");
                    }
                })
                .exceptionally(ex -> {
                    sendResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
                    return null;
                });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Unhandled exception", cause);
        sendResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    private void sendResponse(ChannelHandlerContext ctx, HttpResponseStatus status, Object response) {
        try {
            byte[] jsonBuffer = objectMapper.writeValueAsBytes(response);
            ByteBuf responseBuffer = Unpooled.wrappedBuffer(jsonBuffer);
            HttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, responseBuffer);
            httpResponse.headers()
                    .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                    .set(HttpHeaderNames.CONTENT_LENGTH, jsonBuffer.length);
            ctx.writeAndFlush(httpResponse)
                    .addListener(ChannelFutureListener.CLOSE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
