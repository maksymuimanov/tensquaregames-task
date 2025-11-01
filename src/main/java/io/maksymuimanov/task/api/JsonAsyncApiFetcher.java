package io.maksymuimanov.task.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.maksymuimanov.task.exception.ApiFetchingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

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
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AsyncApiRequestSender<String> requestSender;

    public JsonAsyncApiFetcher(ObjectMapper objectMapper, AsyncApiRequestSender<String> requestSender) {
        this(DEFAULT_HTTP_CLIENT, objectMapper, requestSender);
    }

    @Override
    public CompletableFuture<JsonNode> fetch(String url) {
        try {
            URI uri = URI.create(url);
            HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                    .GET()
                    .timeout(DEFAULT_REQUEST_TIMEOUT);
            String correlationId = MDC.get("correlationId");
            if (correlationId != null) {
                builder.header(CORRELATION_ID_HEADER, correlationId);
            }
            HttpRequest httpRequest = builder.build();
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

    private JsonNode parseJson(String body) {
        try {
            return objectMapper.readTree(body);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse external API JSON body (truncated)");
            throw new ApiFetchingException(e);
        }
    }
}
