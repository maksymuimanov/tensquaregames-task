package io.maksymuimanov.task.exception;

/**
 * Indicates a failure occurred while interacting with the Redis cache during
 * asynchronous API aggregation. This exception wraps lower-level cache errors
 * such as connection issues or serialization problems, ensuring that upstream
 * CompletableFuture pipelines can handle cache failures gracefully.
 *
 * @see io.maksymuimanov.task.cache.AsyncCacheManager
 * @see io.maksymuimanov.task.cache.RedisAsyncCacheManager
 */
public class CacheManagingException extends RuntimeException {
    /**
     * Constructs a new CacheManagingException with the specified underlying cause.
     * Typically thrown when a Redis operation fails during non-blocking API aggregation.
     *
     * @param cause the original exception that triggered the cache failure
     */
    public CacheManagingException(Throwable cause) {
        super(cause);
    }
}
