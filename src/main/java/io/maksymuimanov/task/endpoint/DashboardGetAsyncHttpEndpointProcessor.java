package io.maksymuimanov.task.endpoint;

import io.maksymuimanov.task.api.AsyncApiAggregator;
import io.maksymuimanov.task.cache.AsyncCacheManager;
import io.maksymuimanov.task.dto.DashboardResponse;
import io.maksymuimanov.task.dto.ErrorResponse;
import io.maksymuimanov.task.dto.HttpEndpoint;
import io.maksymuimanov.task.exception.HttpEndpointProcessionException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.concurrent.CompletableFuture;

public class DashboardGetAsyncHttpEndpointProcessor implements AsyncHttpEndpointProcessor {
    public static final String DASHBOARD_ENDPOINT_PATH = "/api/dashboard";
    public static final String DASHBOARD_CACHE_KEY = "dashboard";
    public static final ErrorResponse FAILED_TO_FETCH_DATA_MESSAGE = new ErrorResponse("Failed to fetch data");
    public static final ErrorResponse UNEXPECTED_SERVER_ERROR_MESSAGE = new ErrorResponse("Unexpected server error");
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
    public CompletableFuture<Void> process(ChannelHandlerContext context, HttpResponseSender responseSender, boolean keepAlive) {
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
                            responseSender.send(context, response, HttpResponseStatus.OK, keepAlive);
                        } else {
                            responseSender.send(context, FAILED_TO_FETCH_DATA_MESSAGE, HttpResponseStatus.INTERNAL_SERVER_ERROR, keepAlive);
                        }
                    })
                    .exceptionally(ex -> {
                        responseSender.send(context, UNEXPECTED_SERVER_ERROR_MESSAGE, HttpResponseStatus.INTERNAL_SERVER_ERROR, keepAlive);
                        return null;
                    });
        } catch (Exception e) {
            throw new HttpEndpointProcessionException(e);
        }
    }
}