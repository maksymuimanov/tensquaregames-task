package io.maksymuimanov.task.api;

import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

/**
 * Defines a contract for asynchronously fetching data from external APIs.
 * <p>
 * Implementations of this interface are responsible for performing non-blocking
 * HTTP requests and returning results wrapped in {@link CompletableFuture}.
 * The interface abstracts away the underlying HTTP client and deserialization logic,
 * allowing different response formats (e.g., JSON, XML) or fetching strategies.
 *
 * @param <T> the type of the fetched and deserialized response (e.g., {@code JsonNode})
 *
 * @see JsonAsyncApiFetcher
 */
public interface AsyncApiFetcher<T> {
    /**
     * Asynchronously fetches data from the specified URL.
     * <p>
     * The request is performed in a fully non-blocking manner, completing the
     * returned {@link CompletableFuture} with the parsed response or exceptionally
     * if the operation fails (e.g., due to network or deserialization issues).
     *
     * @param url the target API URL to fetch data from
     * @return a {@link CompletableFuture} that will complete with the fetched result
     */
    @NonNull
    CompletableFuture<T> fetch(@NonNull String url);
}
