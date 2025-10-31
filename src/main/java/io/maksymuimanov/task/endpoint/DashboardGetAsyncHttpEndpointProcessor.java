package io.maksymuimanov.task.endpoint;

import io.maksymuimanov.task.api.AsyncApiAggregator;
import io.maksymuimanov.task.cache.AsyncCacheManager;
import io.maksymuimanov.task.dto.DashboardResponse;
import io.maksymuimanov.task.dto.HttpEndpoint;
import io.maksymuimanov.task.exception.HttpEndpointProcessionException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.concurrent.CompletableFuture;

public class DashboardGetAsyncHttpEndpointProcessor implements AsyncHttpEndpointProcessor {
    public static final String DASHBOARD_ENDPOINT_PATH = "/api/dashboard";
    public static final String DASHBOARD_CACHE_KEY = "dashboard";
    public static final String FAILED_TO_FETCH_DATA_MESSAGE = "Failed to fetch data";
    public static final String UNEXPECTED_SERVER_ERROR_MESSAGE = "Unexpected server error";
    private final AsyncApiAggregator<DashboardResponse> apiAggregator;
    private final AsyncCacheManager cacheManager;
    private final HttpEndpoint endpoint;

    public DashboardGetAsyncHttpEndpointProcessor(AsyncCacheManager cacheManager, AsyncApiAggregator<DashboardResponse> apiAggregator) {
        this.cacheManager = cacheManager;
        this.apiAggregator = apiAggregator;
        this.endpoint = new HttpEndpoint(DASHBOARD_ENDPOINT_PATH, HttpMethod.GET);
    }

    @Override
    public HttpEndpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public CompletableFuture<Void> process(FullHttpRequest request, HttpResponseSender responseSender, ChannelHandlerContext context) {
        try {
            return apiAggregator.aggregate()
                    .exceptionallyCompose(ex -> cacheManager.get(DASHBOARD_CACHE_KEY, DashboardResponse.class)
                            .thenApply(optional -> optional.orElse(null)))
                    .thenCompose(response -> {
                        if (response == null) return CompletableFuture.completedFuture(null);
                        return cacheManager.put(DASHBOARD_CACHE_KEY, response)
                                .thenApply(v -> response);
                    })
                    .thenAccept(response -> {
                        if (response != null) {
                            responseSender.send(context, request, HttpResponseStatus.OK, response);
                        } else {
                            responseSender.send(context, request, HttpResponseStatus.INTERNAL_SERVER_ERROR, FAILED_TO_FETCH_DATA_MESSAGE);
                        }
                    })
                    .exceptionally(ex -> {
                        responseSender.send(context, request, HttpResponseStatus.INTERNAL_SERVER_ERROR, UNEXPECTED_SERVER_ERROR_MESSAGE);
                        return null;
                    });
        } catch (Exception e) {
            throw new HttpEndpointProcessionException(e);
        }
    }
}