package io.maksymuimanov.task.api;

import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public interface AsyncApiFetcher<T> {
    @NonNull
    CompletableFuture<T> fetch(@NonNull String url);
}
