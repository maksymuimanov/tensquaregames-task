package io.maksymuimanov.task.cache;

import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface AsyncCacheManager extends AutoCloseable {
    @NonNull
    <T> CompletableFuture<Optional<T>> get(@NonNull String key, @NonNull Class<T> clazz);

    @NonNull
    CompletableFuture<Void> put(@NonNull String key, Object value);
}
