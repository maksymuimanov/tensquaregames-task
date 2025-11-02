package io.maksymuimanov.task.cache;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Defines a non-blocking, asynchronous cache management contract for storing and retrieving data.
 * <p>
 * Implementations are expected to use reactive or asynchronous APIs (e.g., Redis async commands)
 * to ensure cache operations do not block event loop threads in high-concurrency environments.
 * <p>
 * All methods return {@link CompletableFuture} to support fully asynchronous control flow.
 *
 * @see RedisAsyncCacheManager
 */
public interface AsyncCacheManager extends AutoCloseable {

    /**
     * Retrieves a value from the cache asynchronously by key.
     * <p>
     * If the key is not found or the cached value cannot be deserialized,
     * an empty {@link Optional} is returned instead of failing the future.
     *
     * @param key the cache key to retrieve
     * @param clazz the expected type of the cached value
     * @param <T> the type of the deserialized value
     * @return a {@link CompletableFuture} that completes with an {@link Optional} containing the cached value, or empty if not found
     */
    <T> CompletableFuture<Optional<T>> get(String key, Class<T> clazz);

    /**
     * Stores a value in the cache asynchronously under the specified key.
     * <p>
     * The value is serialized (typically to JSON) before being written.
     * The operation completes when the cache confirms successful storage.
     *
     * @param key the cache key
     * @param value the value to cache
     * @return a {@link CompletableFuture} that completes when the value is stored
     */
    CompletableFuture<Void> put(String key, Object value);
}