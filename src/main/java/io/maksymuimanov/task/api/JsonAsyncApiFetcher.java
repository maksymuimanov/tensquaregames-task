package io.maksymuimanov.task.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.maksymuimanov.task.exception.ApiFetchingException;
import io.maksymuimanov.task.util.ConfigUtils;
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

/**
 * Performs asynchronous HTTP requests to external APIs and parses their responses as JSON.
 * <p>
 * This class provides non-blocking API fetching built on top of {@link HttpClient#sendAsync},
 * using {@link CompletableFuture} for concurrency and {@link ObjectMapper} for JSON parsing.
 * It is responsible for fetching data from external services (e.g., weather, fact, IP APIs)
 * and converting raw responses into {@link JsonNode} structures for downstream aggregation.
 * <p>
 * The implementation uses configurable timeouts and supports virtual-thread-based
 * execution through {@link #DEFAULT_HTTP_EXECUTOR}, ensuring high concurrency
 * and minimal thread blocking.
 *
 * @see AsyncApiRequestSender
 * @see AsyncApiFetcher
 * @see HttpClient
 */
@Slf4j
@RequiredArgsConstructor
public class JsonAsyncApiFetcher implements AsyncApiFetcher<JsonNode> {
    /** System property key defining the HTTP protocol version used for outbound API requests. */
    public static final String API_HTTP_VERSION_PROPERTY = "api.http.version";
    /** System property key defining the connection timeout (in milliseconds) for establishing non-blocking HTTP connections to external APIs. */
    public static final String API_CONNECT_TIMEOUT_PROPERTY = "api.connect.timeout";
    /** System property key defining the total request timeout (in milliseconds)for asynchronous API calls performed by the aggregator. */
    public static final String API_REQUEST_TIMEOUT_PROPERTY = "api.request.timeout";
    /** Default HTTP/2 client version used for non-blocking requests. */
    public static final HttpClient.Version DEFAULT_HTTP_VERSION = ConfigUtils.getOrDefault(API_HTTP_VERSION_PROPERTY, HttpClient.Version.HTTP_2);
    /** Shared executor using virtual threads for concurrent API calls. */
    public static final ExecutorService DEFAULT_HTTP_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    /** Default connection timeout for establishing HTTP connections. */
    public static final Duration DEFAULT_CONNECT_TIMEOUT = ConfigUtils.getOrDefault(API_CONNECT_TIMEOUT_PROPERTY, Duration.ofSeconds(3));
    /** Default HTTP client preconfigured with timeouts and executor. */
    public static final HttpClient DEFAULT_HTTP_CLIENT = HttpClient.newBuilder()
            .version(DEFAULT_HTTP_VERSION)
            .executor(DEFAULT_HTTP_EXECUTOR)
            .connectTimeout(DEFAULT_CONNECT_TIMEOUT)
            .build();
    /** Default per-request timeout for external API calls. */
    public static final Duration DEFAULT_REQUEST_TIMEOUT = ConfigUtils.getOrDefault(API_REQUEST_TIMEOUT_PROPERTY, Duration.ofSeconds(5));
    @NonNull
    private final HttpClient httpClient;
    @NonNull
    private final ObjectMapper objectMapper;
    @NonNull
    private final AsyncApiRequestSender<String> requestSender;

    /**
     * Constructs a {@code JsonAsyncApiFetcher} using a default {@link HttpClient}
     * with HTTP/2, virtual threads, and standard timeouts.
     *
     * @param objectMapper the mapper used to parse JSON responses
     * @param requestSender the asynchronous HTTP request sender
     */
    public JsonAsyncApiFetcher(@NonNull ObjectMapper objectMapper,
                               @NonNull AsyncApiRequestSender<String> requestSender) {
        this(DEFAULT_HTTP_CLIENT, objectMapper, requestSender);
    }

    /**
     * Sends an asynchronous HTTP GET request to the given URL and parses the response body as JSON.
     * <p>
     * If the request fails or the response cannot be parsed, an {@link ApiFetchingException}
     * is thrown. This method completes the returned {@link CompletableFuture} once the
     * external response is available and successfully parsed.
     *
     * @param url the target API URL
     * @return a {@link CompletableFuture} containing the parsed {@link JsonNode} response
     * @throws ApiFetchingException if the API call or JSON parsing fails
     */
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
            return CompletableFuture.failedFuture(new ApiFetchingException(e));
        }
    }

    /**
     * Parses a raw JSON string response into a {@link JsonNode}.
     * <p>
     * Used internally by {@link #fetch(String)} to deserialize HTTP response bodies.
     *
     * @param body the raw JSON body as a string
     * @return the parsed {@link JsonNode} tree
     * @throws ApiFetchingException if the body cannot be parsed as valid JSON
     */
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
