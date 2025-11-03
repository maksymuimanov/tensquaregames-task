package io.maksymuimanov.task.api;

import io.maksymuimanov.task.exception.ApiRequestSendingException;
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
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Handles asynchronous HTTP requests with automatic retry support for transient failures.
 * <p>
 * This sender integrates with {@link HttpClient#sendAsync(HttpRequest, HttpResponse.BodyHandler)}
 * to perform non-blocking HTTP calls and transparently retries failed or unsuccessful responses.
 * It is used by the asynchronous API aggregation layer to ensure reliability when communicating
 * with external services such as weather, fact, or IP providers.
 * <p>
 * Retries are scheduled with a configurable delay using a {@link CompletableFuture} delayed executor.
 *
 * @see AsyncApiRequestSender
 * @see HttpClient
 */
@Slf4j
@RequiredArgsConstructor
public class RetryableAsyncApiRequestSender implements AsyncApiRequestSender<String> {
    /** System property key defining the number of retry attempts for failed asynchronous API requests. */
    public static final String API_REQUEST_RETRY_COUNT_PROPERTY = "api.request.retry.count";
    /** System property key defining the delay (in milliseconds) between retry attempts for failed API requests. */
    public static final String API_REQUEST_RETRY_DELAY_PROPERTY = "api.request.retry.delay";
    /** Default number of retries before giving up on a failed request. */
    public static final int DEFAULT_RETRY_COUNT = ConfigUtils.getOrDefault(API_REQUEST_RETRY_COUNT_PROPERTY, 2);
    /** Default delay between retries. */
    public static final Duration DEFAULT_RETRY_DELAY = ConfigUtils.getOrDefault(API_REQUEST_RETRY_DELAY_PROPERTY, Duration.ofMillis(250));
    /** Inclusive lower bound of successful HTTP status codes. */
    public static final int HTTP_OK_STATUS = 200;
    /** Exclusive upper bound of successful HTTP status codes. */
    public static final int HTTP_SUCCESS_CODE_LIMIT = 300;
    private final int retryCount;
    @NonNull
    private final Duration retryDelay;

    /**
     * Creates a retryable async sender with default retry count and delay.
     */
    public RetryableAsyncApiRequestSender() {
        this(DEFAULT_RETRY_COUNT, DEFAULT_RETRY_DELAY);
    }

    /**
     * Sends an asynchronous HTTP request with automatic retry handling.
     * <p>
     * On failure or non-2xx status codes, retries are triggered up to the configured limit.
     *
     * @param httpClient the {@link HttpClient} used for asynchronous requests
     * @param request the HTTP request to be sent
     * @param handler the response body handler
     * @return a {@link CompletableFuture} that completes with the HTTP response or fails after all retries
     * @throws ApiRequestSendingException if all retries are exhausted and the request still fails
     */
    @Override
    @NonNull
    public CompletableFuture<HttpResponse<String>> send(@NonNull HttpClient httpClient, @NonNull HttpRequest request, HttpResponse.@NonNull BodyHandler<String> handler) {
        URI uri = request.uri();
        log.debug("Sending HTTP request: method={}, uri={}", request.method(), uri);
        return send(httpClient, request, handler, retryCount);
    }

    /**
     * Performs a recursive asynchronous send operation with retry support.
     * <p>
     * Retries are attempted for both connection errors and non-successful HTTP status codes.
     *
     * @param httpClient the client to send requests
     * @param request the HTTP request to send
     * @param handler the response body handler
     * @param retriesLeft number of remaining retry attempts
     * @return a {@link CompletableFuture} that completes with a successful response or fails after retries
     */
    @NonNull
    private CompletableFuture<HttpResponse<String>> send(@NonNull HttpClient httpClient, @NonNull HttpRequest request, HttpResponse.@NonNull BodyHandler<String> handler, int retriesLeft) {
        try {
            return httpClient.sendAsync(request, handler)
                    .exceptionallyCompose(ex -> {
                        if (retriesLeft > 0) {
                            log.warn("Request to {} error: {}, retrying... ({} left)", request.uri(), ex.getMessage(), retriesLeft);
                            return retry(httpClient, request, handler, retriesLeft);
                        }
                        log.error("Request to {} failed with error after retries", request.uri(), ex);
                        return CompletableFuture.failedFuture(new ApiRequestSendingException("Request failed with error: " + ex.getMessage()));
                    })
                    .thenCompose(response -> {
                        int code = response.statusCode();
                        if (code >= HTTP_OK_STATUS && code < HTTP_SUCCESS_CODE_LIMIT) {
                            log.info("Received successful response: uri={}, status={}", request.uri(), code);
                            return CompletableFuture.completedFuture(response);
                        } else if (retriesLeft > 0) {
                            log.warn("Request to {} failed (status={}), retrying... ({} left)", request.uri(), code, retriesLeft);
                            return retry(httpClient, request, handler, retriesLeft);
                        } else {
                            log.error("Request to {} failed with status={} after retries", request.uri(), code);
                            return CompletableFuture.failedFuture(new ApiRequestSendingException("Request failed with status code: " + code));
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(new ApiRequestSendingException(e));
        }
    }

    /**
     * Executes a delayed retry for a failed or unsuccessful request.
     * <p>
     * This method schedules the next retry asynchronously using {@link CompletableFuture#delayedExecutor}.
     *
     * @param httpClient the client to send the next retry
     * @param request the request to retry
     * @param handler the response handler
     * @param retriesLeft remaining retry attempts
     * @return a {@link CompletableFuture} chaining to the next retry attempt
     */
    @NonNull
    private CompletableFuture<HttpResponse<String>> retry(@NonNull HttpClient httpClient, @NonNull HttpRequest request, HttpResponse.@NonNull BodyHandler<String> handler, int retriesLeft) {
        Executor delayedExecutor = CompletableFuture.delayedExecutor(retryDelay.toMillis(), TimeUnit.MILLISECONDS);
        return CompletableFuture.supplyAsync(() -> null, delayedExecutor)
                .thenCompose(v -> send(httpClient, request, handler, retriesLeft - 1));
    }
}
