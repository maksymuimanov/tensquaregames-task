package io.maksymuimanov.task.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.maksymuimanov.task.dto.DashboardResponse;
import io.maksymuimanov.task.exception.ApiAggregationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

/**
 * Aggregates data from multiple asynchronous external APIs into a unified dashboard response.
 * <p>
 * This class concurrently requests weather, fact, and IP information using
 * {@link AsyncApiFetcher}, then combines the results into a single {@link DashboardResponse}.
 * Designed for non-blocking execution with {@link CompletableFuture} to ensure efficient
 * parallel I/O operations.
 *
 * @see AsyncApiFetcher
 * @see AsyncApiAggregator
 */
@Slf4j
@RequiredArgsConstructor
public class DashboardAsyncApiAggregator implements AsyncApiAggregator<DashboardResponse> {
    /** Public weather API providing current conditions for a predefined location. */
    public static final String WEATHER_API_URL = "https://api.open-meteo.com/v1/forecast?latitude=51.107883&longitude=17.038538&current_weather=true";
    /** Public API returning a random useless fact. */
    public static final String FACTS_API_URL = "https://uselessfacts.jsph.pl/api/v2/facts/random";
    /** Public API returning the current external IP address in JSON format. */
    public static final String IP_API_URL = "https://api.ipify.org/?format=json";
    @NonNull
    private final AsyncApiFetcher<JsonNode> asyncApiFetcher;

    /**
     * Concurrently fetches weather, fact, and IP data, then aggregates them
     * into a single {@link DashboardResponse}.
     * <p>
     * All network calls are executed asynchronously using {@link CompletableFuture#allOf},
     * and the combined result is produced once all fetch operations complete.
     * If any request fails, the exception is logged and propagated as an
     * {@link ApiAggregationException}.
     *
     * @return a {@link CompletableFuture} that completes with the aggregated dashboard data
     * @throws ApiAggregationException if a synchronous setup or submission fails
     */
    @Override
    @NonNull
    public CompletableFuture<DashboardResponse> aggregate() {
        try {
            log.info("Starting dashboard aggregation");
            CompletableFuture<JsonNode> weatherResponse = asyncApiFetcher.fetch(WEATHER_API_URL);
            CompletableFuture<JsonNode> factResponse = asyncApiFetcher.fetch(FACTS_API_URL);
            CompletableFuture<JsonNode> ipResponse = asyncApiFetcher.fetch(IP_API_URL);
            return CompletableFuture.allOf(weatherResponse, ipResponse, factResponse)
                    .thenApply(v -> new DashboardResponse(weatherResponse.join(), factResponse.join(), ipResponse.join()))
                    .whenComplete((r, ex) -> {
                        if (ex != null) {
                            log.error("Dashboard aggregation failed", ex);
                        } else {
                            log.info("Dashboard aggregation completed");
                        }
                    });
        } catch (Exception e) {
            log.error("Dashboard aggregation failed (synchronous)", e);
            throw new ApiAggregationException(e);
        }
    }
}
