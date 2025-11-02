package io.maksymuimanov.task.api;

import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

/**
 * Defines a contract for asynchronously aggregating data from multiple sources into
 * a single unified response object.
 * <p>
 * Implementations are expected to orchestrate several non-blocking API calls using
 * {@link CompletableFuture}, combining their results once all are complete.
 * This abstraction enables concurrent data retrieval across distributed services.
 *
 * @param <T> the type of aggregated result returned after all asynchronous operations complete
 *
 * @see DashboardAsyncApiAggregator
 */
public interface AsyncApiAggregator<T> {
    /**
     * Asynchronously aggregates data from multiple APIs or data providers.
     * <p>
     * The method should initiate all required asynchronous calls and return a
     * {@link CompletableFuture} that completes when aggregation is finished.
     *
     * @return a {@link CompletableFuture} producing the final aggregated result
     */
    @NonNull
    CompletableFuture<T> aggregate();
}