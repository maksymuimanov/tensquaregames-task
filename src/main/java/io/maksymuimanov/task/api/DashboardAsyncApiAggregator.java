package io.maksymuimanov.task.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.maksymuimanov.task.dto.DashboardResponse;
import io.maksymuimanov.task.exception.ApiAggregationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class DashboardAsyncApiAggregator implements AsyncApiAggregator<DashboardResponse> {
    public static final String WEATHER_API_URL = "https://api.open-meteo.com/v1/forecast?latitude=51.107883&longitude=17.038538&current_weather=true";
    public static final String FACTS_API_URL = "https://uselessfacts.jsph.pl/api/v2/facts/random";
    public static final String IP_API_URL = "https://api.ipify.org/?format=json";
    @NonNull
    private final AsyncApiFetcher<JsonNode> asyncApiFetcher;

    @Override
    @NonNull
    public CompletableFuture<DashboardResponse> aggregate() {
        try {
            long start = System.currentTimeMillis();
            log.info("Starting dashboard aggregation");
            CompletableFuture<JsonNode> weatherResponse = asyncApiFetcher.fetch(WEATHER_API_URL);
            CompletableFuture<JsonNode> factResponse = asyncApiFetcher.fetch(FACTS_API_URL);
            CompletableFuture<JsonNode> ipResponse = asyncApiFetcher.fetch(IP_API_URL);
            return CompletableFuture.allOf(weatherResponse, ipResponse, factResponse)
                    .thenApply(v -> new DashboardResponse(weatherResponse.join(), factResponse.join(), ipResponse.join()))
                    .whenComplete((r, ex) -> {
                        long took = System.currentTimeMillis() - start;
                        if (ex != null) {
                            log.error("Dashboard aggregation failed after {} ms", took, ex);
                        } else {
                            log.info("Dashboard aggregation completed in {} ms", took);
                        }
                    });
        } catch (Exception e) {
            log.error("Dashboard aggregation failed (synchronous)", e);
            throw new ApiAggregationException(e);
        }
    }
}
