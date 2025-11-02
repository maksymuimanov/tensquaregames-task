package api;

import com.fasterxml.jackson.databind.JsonNode;
import io.maksymuimanov.task.api.AsyncApiAggregator;
import io.maksymuimanov.task.api.AsyncApiFetcher;
import io.maksymuimanov.task.api.DashboardAsyncApiAggregator;
import io.maksymuimanov.task.dto.DashboardResponse;
import io.maksymuimanov.task.exception.ApiAggregationException;
import io.maksymuimanov.task.exception.ApiFetchingException;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unchecked")
class DashboardAsyncApiAggregatorTests {
    AsyncApiAggregator<DashboardResponse> asyncApiAggregator;
    AsyncApiFetcher<JsonNode> asyncApiFetcher;
    JsonNode weatherResponse;
    JsonNode factResponse;
    JsonNode ipResponse;

    @BeforeEach
    void setUp() {
        asyncApiFetcher = Mockito.mock(AsyncApiFetcher.class);
        asyncApiAggregator = new DashboardAsyncApiAggregator(asyncApiFetcher);
    }

    @Test
    void shouldAggregateSuccessfully() {
        Mockito.when(asyncApiFetcher.fetch(DashboardAsyncApiAggregator.WEATHER_API_URL)).thenReturn(CompletableFuture.completedFuture(weatherResponse));
        Mockito.when(asyncApiFetcher.fetch(DashboardAsyncApiAggregator.FACTS_API_URL)).thenReturn(CompletableFuture.completedFuture(factResponse));
        Mockito.when(asyncApiFetcher.fetch(DashboardAsyncApiAggregator.IP_API_URL)).thenReturn(CompletableFuture.completedFuture(ipResponse));

        CompletableFuture<DashboardResponse> result = asyncApiAggregator.aggregate();
        Awaitility.await().untilAsserted(result::isDone);
        Mockito.verify(asyncApiFetcher, Mockito.times(3)).fetch(Mockito.anyString());
        DashboardResponse response = result.join();
        Assertions.assertEquals(response.weather(), weatherResponse);
        Assertions.assertEquals(response.fact(), factResponse);
        Assertions.assertEquals(response.ip(), ipResponse);
    }

    @Test
    void shouldOneApiFailToAggregate() {
        Mockito.when(asyncApiFetcher.fetch(DashboardAsyncApiAggregator.WEATHER_API_URL)).thenReturn(CompletableFuture.completedFuture(weatherResponse));
        Mockito.when(asyncApiFetcher.fetch(DashboardAsyncApiAggregator.FACTS_API_URL)).thenReturn(CompletableFuture.failedFuture(new ApiFetchingException(new RuntimeException("Test exception"))));
        Mockito.when(asyncApiFetcher.fetch(DashboardAsyncApiAggregator.IP_API_URL)).thenReturn(CompletableFuture.completedFuture(ipResponse));

        CompletableFuture<DashboardResponse> result = asyncApiAggregator.aggregate();
        Awaitility.await().untilAsserted(result::isCompletedExceptionally);
        Mockito.verify(asyncApiFetcher, Mockito.times(3)).fetch(Mockito.anyString());
    }

    @Test
    void shouldFailToAggregate() {
        Mockito.when(asyncApiFetcher.fetch(DashboardAsyncApiAggregator.WEATHER_API_URL)).thenReturn(CompletableFuture.completedFuture(weatherResponse));
        Mockito.when(asyncApiFetcher.fetch(DashboardAsyncApiAggregator.FACTS_API_URL)).thenThrow(new ApiFetchingException(new RuntimeException("Test exception")));
        Mockito.when(asyncApiFetcher.fetch(DashboardAsyncApiAggregator.IP_API_URL)).thenReturn(CompletableFuture.completedFuture(ipResponse));

        Assertions.assertThrows(ApiAggregationException.class, () -> asyncApiAggregator.aggregate());
    }
}
