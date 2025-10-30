package io.maksymuimanov.task.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.maksymuimanov.task.api.AsyncApiAggregator;
import io.maksymuimanov.task.api.DashboardApiAggregator;
import io.maksymuimanov.task.cache.AsyncCacheManager;
import io.maksymuimanov.task.cache.RedisAsyncCacheManager;
import io.maksymuimanov.task.dto.DashboardResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.util.concurrent.CompletableFuture;

public class DashboardServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    public static final String DASHBOARD_ENDPOINT = "/api/dashboard";
    public static final String DASHBOARD_CACHE_KEY = "dashboard";
    private final AsyncApiAggregator<DashboardResponse> apiAggregator;
    private final ObjectMapper objectMapper;
    private final AsyncCacheManager cacheManager;

    public DashboardServerHandler() {
        this(new DashboardApiAggregator(), new ObjectMapper(), new RedisAsyncCacheManager());
    }

    public DashboardServerHandler(AsyncApiAggregator<DashboardResponse> apiAggregator, ObjectMapper objectMapper, AsyncCacheManager cacheManager) {
        this.apiAggregator = apiAggregator;
        this.objectMapper = objectMapper;
        this.cacheManager = cacheManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
        if (!DASHBOARD_ENDPOINT.equals(msg.uri())) {
            this.sendResponse(ctx, HttpResponseStatus.NOT_FOUND, "Not Found");
            return;
        }

        try (cacheManager) {
            apiAggregator.aggregate()
                    .handle((response, ex) -> {
                        if (ex != null || response == null) {
                            return cacheManager.get(DASHBOARD_CACHE_KEY, DashboardResponse.class)
                                    .thenApply(optional -> optional.orElse(null));
                        } else {
                            return CompletableFuture.completedFuture(response);
                        }
                    })
                    .thenCompose(future -> future)
                    .thenCompose(response -> {
                        if (response != null) return cacheManager.put(DASHBOARD_CACHE_KEY, response)
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
                        ex.printStackTrace();
                        sendResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
                        return null;
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//        cause.printStackTrace();
//        ctx.close();
//    }

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
