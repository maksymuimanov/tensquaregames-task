package io.maksymuimanov.task.api;

import io.maksymuimanov.task.exception.ApiRequestSendingException;
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

@Slf4j
@RequiredArgsConstructor
public class RetryableAsyncApiRequestSender implements AsyncApiRequestSender<String> {
    public static final int DEFAULT_RETRY_COUNT = 2;
    public static final Duration DEFAULT_RETRY_DELAY = Duration.ofMillis(250);
    public static final int HTTP_OK_STATUS = 200;
    public static final int HTTP_SUCCESS_CODE_LIMIT = 300;
    private final int retryCount;
    @NonNull
    private final Duration retryDelay;

    public RetryableAsyncApiRequestSender() {
        this(DEFAULT_RETRY_COUNT, DEFAULT_RETRY_DELAY);
    }

    @Override
    @NonNull
    public CompletableFuture<HttpResponse<String>> send(@NonNull HttpClient httpClient, @NonNull HttpRequest request, HttpResponse.@NonNull BodyHandler<String> handler) {
        URI uri = request.uri();
        log.debug("Sending HTTP request: method={}, uri={}", request.method(), uri);
        return send(httpClient, request, handler, retryCount);
    }

    @NonNull
    private CompletableFuture<HttpResponse<String>> send(@NonNull HttpClient httpClient, @NonNull HttpRequest request, HttpResponse.@NonNull BodyHandler<String> handler, int retriesLeft) {
        return httpClient.sendAsync(request, handler)
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
                })
                .exceptionallyCompose(ex -> {
                    if (retriesLeft > 0) {
                        log.warn("Request to {} error: {}, retrying... ({} left)", request.uri(), ex.getMessage(), retriesLeft);
                        return retry(httpClient, request, handler, retriesLeft);
                    }
                    log.error("Request to {} failed with error after retries", request.uri(), ex);
                    return CompletableFuture.failedFuture(ex);
                });
    }

    @NonNull
    private CompletableFuture<HttpResponse<String>> retry(@NonNull HttpClient httpClient, @NonNull HttpRequest request, HttpResponse.@NonNull BodyHandler<String> handler, int retriesLeft) {
        Executor delayedExecutor = CompletableFuture.delayedExecutor(retryDelay.toMillis(), TimeUnit.MILLISECONDS);
        return CompletableFuture.supplyAsync(() -> null, delayedExecutor)
                .thenCompose(v -> send(httpClient, request, handler, retriesLeft - 1));
    }
}
