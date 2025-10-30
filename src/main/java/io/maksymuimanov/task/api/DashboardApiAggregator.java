package io.maksymuimanov.task.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.maksymuimanov.task.dto.DashboardResponse;

import java.util.concurrent.CompletableFuture;

public class DashboardApiAggregator implements AsyncApiAggregator<DashboardResponse> {
    public static final String WEATHER_API_URL = "https://api.open-meteo.com/v1/forecast?latitude=51.107883&longitude=17.038538&current_weather=true";
    public static final String FACTS_API_URL = "https://uselessfacts.jsph.pl/api/v2/facts/random";
    public static final String IP_API_URL = "https://api.ipify.org/?format=json";
    private final AsyncApiFetcher<JsonNode> asyncApiFetcher;

    public DashboardApiAggregator() {
        this(new JsonAsyncApiFetcher());
    }

    public DashboardApiAggregator(AsyncApiFetcher<JsonNode> asyncApiFetcher) {
        this.asyncApiFetcher = asyncApiFetcher;
    }

    @Override
    public CompletableFuture<DashboardResponse> aggregate() {
        CompletableFuture<JsonNode> weatherResponse = asyncApiFetcher.fetch(WEATHER_API_URL);
        CompletableFuture<JsonNode> factResponse = asyncApiFetcher.fetch(FACTS_API_URL);
        CompletableFuture<JsonNode> ipResponse = asyncApiFetcher.fetch(IP_API_URL);
        return CompletableFuture.allOf(weatherResponse, ipResponse, factResponse)
                .thenApply(v -> new DashboardResponse(weatherResponse.join(), factResponse.join(), ipResponse.join()));
    }
}
