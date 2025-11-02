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

/**
 * Handles asynchronous processing of the {@code GET /api/dashboard} endpoint.
 * <p>
 * This processor orchestrates concurrent API aggregation, resilient caching via Redis,
 * and non-blocking HTTP response delivery through Netty. It first attempts to fetch
 * live data from multiple APIs and falls back to cached data if an error occurs.
 *
 * @see AsyncApiAggregator
 * @see AsyncCacheManager
 * @see AsyncHttpEndpointProcessor
 * @see HttpResponseSender
 */
@Slf4j
public class DashboardGetAsyncHttpEndpointProcessor implements AsyncHttpEndpointProcessor {
    /** Endpoint path for the dashboard API. */
    public static final String DASHBOARD_ENDPOINT_PATH = "/api/dashboard";
    /** Descriptor of the HTTP endpoint handled by this processor. */
    public static final HttpEndpoint DASHBOARD_HTTP_ENDPOINT = new HttpEndpoint(DASHBOARD_ENDPOINT_PATH, HttpMethod.GET);
    /** Redis key used for caching aggregated dashboard data. */
    public static final String DASHBOARD_CACHE_KEY = "dashboard";
    /** Generic response returned when all data fetch attempts fail. */
    public static final ErrorResponse FAILED_TO_FETCH_DATA_MESSAGE = new ErrorResponse("Failed to fetch data");
    /** Generic response returned for unexpected server-side errors. */
    public static final ErrorResponse UNEXPECTED_SERVER_ERROR_MESSAGE = new ErrorResponse("Unexpected server error");
    @NonNull
    private final AsyncApiAggregator<DashboardResponse> apiAggregator;
    @NonNull
    private final AsyncCacheManager cacheManager;

    /**
     * Creates a new asynchronous dashboard endpoint processor.
     *
     * @param cacheManager   asynchronous cache manager for Redis storage
     * @param apiAggregator  concurrent aggregator fetching data from multiple APIs
     */
    public DashboardGetAsyncHttpEndpointProcessor(@NonNull AsyncCacheManager cacheManager,
                                                  @NonNull AsyncApiAggregator<DashboardResponse> apiAggregator) {
        this.cacheManager = cacheManager;
        this.apiAggregator = apiAggregator;
    }

    /**
     * Returns the HTTP endpoint handled by this processor.
     *
     * @return descriptor for {@code GET /api/dashboard}
     */
    @Override
    @NonNull
    public HttpEndpoint getEndpoint() {
        return DASHBOARD_HTTP_ENDPOINT;
    }

    /**
     * Processes a dashboard request asynchronously.
     * <p>
     * Attempts to aggregate data from remote APIs and cache it.
     * If the aggregation fails, cached data is used as a fallback.
     * The response is then written back to the client using Netty.
     *
     * @param context Netty context for writing the response
     * @param responseSender component responsible for serializing and sending JSON responses
     * @param keepAlive whether to keep the connection open after sending
     * @return a {@link CompletableFuture} completing when the response has been sent
     * @throws HttpEndpointProcessionException if a fatal synchronous error occurs
     */
    @Override
    @NonNull
    public CompletableFuture<Void> process(@NonNull ChannelHandlerContext context, @NonNull HttpResponseSender responseSender, boolean keepAlive) {
        try {
            log.info("Processing dashboard endpoint");
            return apiAggregator.aggregate()
                    .handle((response, ex) -> {
                        if (ex == null) return response;
                        log.warn("Aggregation failed, attempting to use cached dashboard: {}", ex.getMessage());
                        return null;
                    })
                    .thenCompose(response -> {
                        if (response == null) return cacheManager.get(DASHBOARD_CACHE_KEY, DashboardResponse.class)
                                .handle((optional, cacheEx) -> cacheEx == null ? optional.orElse(null) : null);
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
            log.error("Something went wrong in dashboard processing: ", e);
            return CompletableFuture.failedFuture(new HttpEndpointProcessionException(e));
        }
    }
}