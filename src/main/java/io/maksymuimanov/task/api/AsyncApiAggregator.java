package io.maksymuimanov.task.api;

import java.util.concurrent.CompletableFuture;

public interface AsyncApiAggregator<T> {
    CompletableFuture<T> aggregate();
}