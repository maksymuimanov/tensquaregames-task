package io.maksymuimanov.task.api;

import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public interface AsyncApiAggregator<T> {
    @NonNull
    CompletableFuture<T> aggregate();
}