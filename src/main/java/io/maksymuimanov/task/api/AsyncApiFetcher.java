package io.maksymuimanov.task.api;

import java.util.concurrent.CompletableFuture;

public interface AsyncApiFetcher<T> {
    CompletableFuture<T> fetch(String url);
}
