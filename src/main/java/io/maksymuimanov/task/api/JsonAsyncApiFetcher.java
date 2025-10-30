package io.maksymuimanov.task.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class JsonAsyncApiFetcher implements AsyncApiFetcher<JsonNode> {
    protected static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .executor(Executors.newVirtualThreadPerTaskExecutor())
            .build();
    private final ObjectMapper objectMapper;

    public JsonAsyncApiFetcher() {
        this(new ObjectMapper());
    }

    public JsonAsyncApiFetcher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public CompletableFuture<JsonNode> fetch(String url) {
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse.BodyHandler<String> stringBodyHandler = HttpResponse.BodyHandlers.ofString();
        return HTTP_CLIENT.sendAsync(httpRequest, stringBodyHandler)
                .thenApply(HttpResponse::body)
                .thenApply(this::parseJson);
    }

    private JsonNode parseJson(String body) {
        try {
            return objectMapper.readTree(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
