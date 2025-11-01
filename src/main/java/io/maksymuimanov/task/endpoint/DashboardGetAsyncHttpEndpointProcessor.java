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
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class DashboardGetAsyncHttpEndpointProcessor implements AsyncHttpEndpointProcessor {
    public static final String DASHBOARD_ENDPOINT_PATH = "/api/dashboard";
    public static final String DASHBOARD_CACHE_KEY = "dashboard";
    public static final ErrorResponse FAILED_TO_FETCH_DATA_MESSAGE = new ErrorResponse("Failed to fetch data");
    public static final ErrorResponse UNEXPECTED_SERVER_ERROR_MESSAGE = new ErrorResponse("Unexpected server error");
    @NonNull
    private final AsyncApiAggregator<DashboardResponse> apiAggregator;
    @NonNull
    private final AsyncCacheManager cacheManager;
    @NonNull
    private final HttpEndpoint endpoint;

    public DashboardGetAsyncHttpEndpointProcessor(@NonNull AsyncCacheManager cacheManager,
                                                  @NonNull AsyncApiAggregator<DashboardResponse> apiAggregator) {
        this.cacheManager = cacheManager;
        this.apiAggregator = apiAggregator;
        this.endpoint = new HttpEndpoint(DASHBOARD_ENDPOINT_PATH, HttpMethod.GET);
    }

    @Override
    @NonNull
    public HttpEndpoint getEndpoint() {
        return endpoint;
    }

    @Override
    @NonNull
    public CompletableFuture<Void> process(@NonNull ChannelHandlerContext context, @NonNull HttpResponseSender responseSender, boolean keepAlive) {
        try {
            log.info("Processing dashboard endpoint");
            return apiAggregator.aggregate()
                    .exceptionallyCompose(ex -> {
                        log.warn("Aggregation failed, attempting to use cached dashboard: {}", ex.getMessage());
                        return cacheManager.get(DASHBOARD_CACHE_KEY, DashboardResponse.class)
                                .thenApply(optional -> optional.orElse(null));
                    })
                    .thenCompose(response -> {
                        if (response == null) return CompletableFuture.completedFuture(null);
                        return cacheManager.put(DASHBOARD_CACHE_KEY, response)
                                .thenApply(v -> response);
                    })
                    .thenAccept(response -> {
                        if (response != null) {
                            log.info("Dashboard processed successfully");
                            responseSender.send(context, response, HttpResponseStatus.OK, keepAlive);
                        } else {
                            log.error("Dashboard processing failed: no data available");
                            responseSender.send(context, FAILED_TO_FETCH_DATA_MESSAGE, HttpResponseStatus.INTERNAL_SERVER_ERROR, keepAlive);
                        }
                    })
                    .exceptionally(ex -> {
                        log.error("Unexpected error while processing dashboard", ex);
                        responseSender.send(context, UNEXPECTED_SERVER_ERROR_MESSAGE, HttpResponseStatus.INTERNAL_SERVER_ERROR, keepAlive);
                        return null;
                    });
        } catch (Exception e) {
            log.error("Synchronous error in dashboard processing", e);
            throw new HttpEndpointProcessionException(e);
        }
    }
}