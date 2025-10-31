package io.maksymuimanov.task.api;

import io.maksymuimanov.task.exception.ApiRequestSendingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final Duration retryDelay;

    public RetryableAsyncApiRequestSender() {
        this(DEFAULT_RETRY_COUNT, DEFAULT_RETRY_DELAY);
    }

    @Override
    public CompletableFuture<HttpResponse<String>> send(HttpClient httpClient, HttpRequest request, HttpResponse.BodyHandler<String> handler) {
        return send(httpClient, request, handler, retryCount);
    }

    private CompletableFuture<HttpResponse<String>> send(HttpClient httpClient, HttpRequest request, HttpResponse.BodyHandler<String> handler, int retriesLeft) {
        return httpClient.sendAsync(request, handler)
                .thenCompose(response -> {
                    int code = response.statusCode();
                    if (code >= HTTP_OK_STATUS && code < HTTP_SUCCESS_CODE_LIMIT) {
                        return CompletableFuture.completedFuture(response);
                    } else if (retriesLeft > 0) {
                        log.warn("Request failed ({}), retrying... ({} left)", code, retriesLeft);
                        return retry(httpClient, request, handler, retriesLeft);
                    } else {
                        return CompletableFuture.failedFuture(new ApiRequestSendingException("Request failed with status code: " + code));
                    }
                })
                .exceptionallyCompose(ex -> {
                    if (retriesLeft > 0) {
                        log.warn("Request error: {}, retrying... ({} left)", ex.getMessage(), retriesLeft);
                        return retry(httpClient, request, handler, retriesLeft);
                    }
                    return CompletableFuture.failedFuture(ex);
                });
    }

    private CompletableFuture<HttpResponse<String>> retry(HttpClient httpClient, HttpRequest request, HttpResponse.BodyHandler<String> handler, int retriesLeft) {
        Executor delayedExecutor = CompletableFuture.delayedExecutor(retryDelay.toMillis(), TimeUnit.MILLISECONDS);
        return CompletableFuture.supplyAsync(() -> null, delayedExecutor)
                .thenCompose(v -> send(httpClient, request, handler, retriesLeft - 1));
    }
}
