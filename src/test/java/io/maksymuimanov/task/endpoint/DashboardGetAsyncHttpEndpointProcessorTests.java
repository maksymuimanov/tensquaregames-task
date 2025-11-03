package io.maksymuimanov.task.endpoint;

import io.maksymuimanov.task.api.AsyncApiAggregator;
import io.maksymuimanov.task.cache.AsyncCacheManager;
import io.maksymuimanov.task.dto.DashboardResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@SuppressWarnings("unchecked")
class DashboardGetAsyncHttpEndpointProcessorTests {
    static final boolean NOT_KEEP_ALIVE = false;
    AsyncHttpEndpointProcessor dashboardGetAsyncHttpEndpointProcessor;
    AsyncApiAggregator<DashboardResponse> apiAggregator;
    AsyncCacheManager cacheManager;
    ChannelHandlerContext context;
    HttpResponseSender responseSender;
    DashboardResponse dashboardResponse;

    @BeforeEach
    void setUp() {
        apiAggregator = Mockito.mock(AsyncApiAggregator.class);
        cacheManager = Mockito.mock(AsyncCacheManager.class);
        context = Mockito.mock(ChannelHandlerContext.class);
        responseSender = Mockito.mock(HttpResponseSender.class);
        dashboardResponse = Mockito.mock(DashboardResponse.class);
        dashboardGetAsyncHttpEndpointProcessor = new DashboardGetAsyncHttpEndpointProcessor(cacheManager, apiAggregator);
    }

    @Test
    void shouldProcessWithAggregationSuccessfully() {
        CompletableFuture<DashboardResponse> responseFuture = CompletableFuture.completedFuture(dashboardResponse);
        CompletableFuture<Void> voidFuture = CompletableFuture.completedFuture(null);

        Mockito.when(apiAggregator.aggregate()).thenReturn(responseFuture);
        Mockito.when(cacheManager.put(DashboardGetAsyncHttpEndpointProcessor.DASHBOARD_CACHE_KEY, dashboardResponse)).thenReturn(voidFuture);

        CompletableFuture<Void> result = dashboardGetAsyncHttpEndpointProcessor.process(context, responseSender, NOT_KEEP_ALIVE);
        Awaitility.await().untilAsserted(result::isDone);
        Mockito.verify(apiAggregator).aggregate();
        Mockito.verify(cacheManager, Mockito.never()).get(DashboardGetAsyncHttpEndpointProcessor.DASHBOARD_CACHE_KEY, DashboardResponse.class);
        Mockito.verify(cacheManager).put(DashboardGetAsyncHttpEndpointProcessor.DASHBOARD_CACHE_KEY, dashboardResponse);
        Mockito.verify(responseSender).send(context, dashboardResponse, HttpResponseStatus.OK, NOT_KEEP_ALIVE);
    }

    @Test
    void shouldProcessWithCacheSuccessfully() {
        CompletableFuture<DashboardResponse> failedFuture = CompletableFuture.failedFuture(new RuntimeException("Test exception"));
        CompletableFuture<Optional<DashboardResponse>> optionalResponseFuture = CompletableFuture.completedFuture(Optional.of(dashboardResponse));
        CompletableFuture<Void> voidFuture = CompletableFuture.completedFuture(null);

        Mockito.when(apiAggregator.aggregate()).thenReturn(failedFuture);
        Mockito.when(cacheManager.get(DashboardGetAsyncHttpEndpointProcessor.DASHBOARD_CACHE_KEY, DashboardResponse.class)).thenReturn(optionalResponseFuture);
        Mockito.when(cacheManager.put(DashboardGetAsyncHttpEndpointProcessor.DASHBOARD_CACHE_KEY, dashboardResponse)).thenReturn(voidFuture);

        CompletableFuture<Void> result = dashboardGetAsyncHttpEndpointProcessor.process(context, responseSender, NOT_KEEP_ALIVE);
        Awaitility.await().untilAsserted(result::isDone);
        Mockito.verify(apiAggregator).aggregate();
        Mockito.verify(cacheManager).get(DashboardGetAsyncHttpEndpointProcessor.DASHBOARD_CACHE_KEY, DashboardResponse.class);
        Mockito.verify(cacheManager, Mockito.never()).put(DashboardGetAsyncHttpEndpointProcessor.DASHBOARD_CACHE_KEY, dashboardResponse);
        Mockito.verify(responseSender).send(context, dashboardResponse, HttpResponseStatus.OK, NOT_KEEP_ALIVE);
    }

    @Test
    void shouldProcessWithFailedToFetchData() {
        CompletableFuture<DashboardResponse> failedFuture = CompletableFuture.failedFuture(new RuntimeException("Test exception"));
        CompletableFuture<Optional<DashboardResponse>> optionalEmptyResponseFuture = CompletableFuture.completedFuture(Optional.empty());

        Mockito.when(apiAggregator.aggregate()).thenReturn(failedFuture);
        Mockito.when(cacheManager.get(DashboardGetAsyncHttpEndpointProcessor.DASHBOARD_CACHE_KEY, DashboardResponse.class)).thenReturn(optionalEmptyResponseFuture);

        CompletableFuture<Void> result = dashboardGetAsyncHttpEndpointProcessor.process(context, responseSender, NOT_KEEP_ALIVE);
        Awaitility.await().untilAsserted(result::isDone);
        Mockito.verify(apiAggregator).aggregate();
        Mockito.verify(cacheManager).get(DashboardGetAsyncHttpEndpointProcessor.DASHBOARD_CACHE_KEY, DashboardResponse.class);
        Mockito.verify(cacheManager, Mockito.never()).put(DashboardGetAsyncHttpEndpointProcessor.DASHBOARD_CACHE_KEY, dashboardResponse);
        Mockito.verify(responseSender).send(context, DashboardGetAsyncHttpEndpointProcessor.FAILED_TO_FETCH_DATA_MESSAGE, HttpResponseStatus.INTERNAL_SERVER_ERROR, NOT_KEEP_ALIVE);
    }

    @Test
    void shouldProcessWithUnexpectedServerError() {
        CompletableFuture<DashboardResponse> responseFuture = CompletableFuture.completedFuture(dashboardResponse);
        CompletableFuture<Void> failedFuture = CompletableFuture.failedFuture(new RuntimeException("Test exception"));

        Mockito.when(apiAggregator.aggregate()).thenReturn(responseFuture);
        Mockito.when(cacheManager.put(DashboardGetAsyncHttpEndpointProcessor.DASHBOARD_CACHE_KEY, dashboardResponse)).thenReturn(failedFuture);

        CompletableFuture<Void> result = dashboardGetAsyncHttpEndpointProcessor.process(context, responseSender, NOT_KEEP_ALIVE);
        Awaitility.await().untilAsserted(result::isDone);
        Mockito.verify(apiAggregator).aggregate();
        Mockito.verify(cacheManager, Mockito.never()).get(DashboardGetAsyncHttpEndpointProcessor.DASHBOARD_CACHE_KEY, DashboardResponse.class);
        Mockito.verify(cacheManager).put(DashboardGetAsyncHttpEndpointProcessor.DASHBOARD_CACHE_KEY, dashboardResponse);
        Mockito.verify(responseSender).send(context, DashboardGetAsyncHttpEndpointProcessor.UNEXPECTED_SERVER_ERROR_MESSAGE, HttpResponseStatus.INTERNAL_SERVER_ERROR, NOT_KEEP_ALIVE);
    }

    @Test
    void shouldFailToProcess() {
        Mockito.when(apiAggregator.aggregate()).thenThrow(RuntimeException.class);

        Assertions.assertThrows(CompletionException.class, () -> dashboardGetAsyncHttpEndpointProcessor.process(context, responseSender, NOT_KEEP_ALIVE).join());
    }
}
