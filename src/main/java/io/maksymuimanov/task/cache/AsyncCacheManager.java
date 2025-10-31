package io.maksymuimanov.task.cache;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface AsyncCacheManager extends AutoCloseable {
    <T> CompletableFuture<Optional<T>> get(String key, Class<T> clazz);

    CompletableFuture<Void> put(String key, Object value);

    CompletableFuture<Void> remove(String key);

    @Override
    void close();
}
