package io.maksymuimanov.task.api;

import org.jspecify.annotations.NonNull;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Defines a contract for asynchronously sending HTTP requests in a non-blocking manner.
 * <p>
 * Implementations are responsible for performing network I/O using {@link HttpClient#sendAsync}
 * and returning results as {@link CompletableFuture} instances. This interface abstracts away
 * retry logic, error handling, and response transformation for external API calls.
 * <p>
 * Typical implementations (e.g., {@link RetryableAsyncApiRequestSender})
 * ensure resilience when communicating with unreliable third-party services.
 *
 * @param <T> the type of the HTTP response body
 *
 * @see RetryableAsyncApiRequestSender
 * @see HttpClient
 */
public interface AsyncApiRequestSender<T> {
    /**
     * Sends an asynchronous HTTP request using a provided {@link HttpClient}.
     * <p>
     * The request is executed in a fully non-blocking fashion, returning a {@link CompletableFuture}
     * that completes when the response is available or exceptionally if the call fails.
     *
     * @param httpClient the client instance used to send the request
     * @param request the HTTP request to send
     * @param handler the response body handler determining how to process the response body
     * @return a {@link CompletableFuture} representing the pending result of the HTTP call
     */
    @NonNull
    CompletableFuture<HttpResponse<T>> send(@NonNull HttpClient httpClient, @NonNull HttpRequest request, HttpResponse.@NonNull BodyHandler<T> handler);
}