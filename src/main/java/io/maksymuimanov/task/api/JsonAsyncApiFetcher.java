package io.maksymuimanov.task.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.maksymuimanov.task.exception.ApiFetchingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
public class JsonAsyncApiFetcher implements AsyncApiFetcher<JsonNode> {
    public static final HttpClient.Version DEFAULT_HTTP_VERSION = HttpClient.Version.HTTP_2;
    public static final ExecutorService DEFAULT_HTTP_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(3);
    public static final HttpClient DEFAULT_HTTP_CLIENT = HttpClient.newBuilder()
            .version(DEFAULT_HTTP_VERSION)
            .executor(DEFAULT_HTTP_EXECUTOR)
            .connectTimeout(DEFAULT_CONNECT_TIMEOUT)
            .build();
    public static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(5);
    @NonNull
    private final HttpClient httpClient;
    @NonNull
    private final ObjectMapper objectMapper;
    @NonNull
    private final AsyncApiRequestSender<String> requestSender;

    public JsonAsyncApiFetcher(@NonNull ObjectMapper objectMapper,
                               @NonNull AsyncApiRequestSender<String> requestSender) {
        this(DEFAULT_HTTP_CLIENT, objectMapper, requestSender);
    }

    @Override
    @NonNull
    public CompletableFuture<JsonNode> fetch(@NonNull String url) {
        try {
            URI uri = URI.create(url);
            HttpRequest httpRequest = HttpRequest.newBuilder(uri)
                    .GET()
                    .timeout(DEFAULT_REQUEST_TIMEOUT)
                    .build();
            log.info("Fetching external API: uri={}", uri);
            HttpResponse.BodyHandler<String> stringBodyHandler = HttpResponse.BodyHandlers.ofString();
            return requestSender.send(httpClient, httpRequest, stringBodyHandler)
                    .thenApply(HttpResponse::body)
                    .thenApply(this::parseJson);
        } catch (Exception e) {
            log.error("Failed to fetch external API: url={}", url, e);
            throw new ApiFetchingException(e);
        }
    }

    @NonNull
    private JsonNode parseJson(String body) {
        try {
            return objectMapper.readTree(body);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse external API JSON body");
            throw new ApiFetchingException(e);
        }
    }
}
