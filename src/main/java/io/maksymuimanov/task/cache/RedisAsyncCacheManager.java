package io.maksymuimanov.task.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.maksymuimanov.task.exception.CacheManagingException;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Provides asynchronous caching using Redis for non-blocking API aggregation operations.
 * <p>
 * This manager handles JSON serialization, cache retrieval, and cache storage through
 * the Lettuce asynchronous Redis client. It is designed to integrate seamlessly into
 * the concurrent Netty-based system, supporting resilience through cached fallbacks
 * and non-blocking cache access.
 *
 * @see AsyncCacheManager
 */
@Slf4j
public class RedisAsyncCacheManager implements AsyncCacheManager {
    /** Default Redis server connection URI used when no external configuration is provided. */
    public static final String DEFAULT_REDIS_URL = "redis://localhost:6379";
    /** Default cache entry time-to-live (TTL), applied to all entries unless overridden. */
    public static final Duration DEFAULT_TTL = Duration.ofMinutes(5);
    /** System property key for customizing the Redis connection URL at runtime. */
    public static final String REDIS_URL_PROPERTY = "redis.url";
    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;
    private final RedisAsyncCommands<String, String> commands;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    /**
     * Creates a cache manager using the Redis URL from system properties or the default value.
     *
     * @param objectMapper Jackson mapper for JSON serialization and deserialization.
     */
    public RedisAsyncCacheManager(ObjectMapper objectMapper) {
        this(System.getProperty(REDIS_URL_PROPERTY, DEFAULT_REDIS_URL), objectMapper);
    }

    /**
     * Creates a Redis-based cache manager with a default TTL.
     *
     * @param url Redis server URL.
     * @param objectMapper Jackson mapper for JSON serialization and deserialization.
     */
    public RedisAsyncCacheManager(String url,
                                  ObjectMapper objectMapper) {
        this(url, objectMapper, DEFAULT_TTL);
    }

    /**
     * Creates a Redis-based cache manager with custom TTL.
     *
     * @param url Redis server URL.
     * @param objectMapper Jackson mapper for JSON serialization and deserialization.
     * @param ttl Default expiration time for stored values.
     */
    public RedisAsyncCacheManager(String url,
                                  ObjectMapper objectMapper,
                                  Duration ttl) {
        this(RedisClient.create(url), objectMapper, ttl);
    }

    /**
     * Initializes Redis client connection and asynchronous command interface.
     *
     * @param redisClient Redis client instance.
     * @param objectMapper Jackson mapper for JSON serialization and deserialization.
     * @param ttl Default expiration time for stored values.
     */
    public RedisAsyncCacheManager(RedisClient redisClient,
                                  ObjectMapper objectMapper,
                                  Duration ttl) {
        this.redisClient = redisClient;
        this.connection = redisClient.connect();
        this.commands = this.connection.async();
        this.objectMapper = objectMapper;
        this.ttl = ttl;
        log.info("Initialized Redis cache manager with TTL={}s", ttl.toSeconds());
    }

    /**
     * Asynchronously retrieves a value from Redis cache and deserializes it into the specified type.
     *
     * @param key Cache key.
     * @param clazz Type of value to deserialize.
     * @param <T> Type parameter.
     * @return A {@link CompletableFuture} with an {@link Optional} value, empty if not found or invalid.
     * @throws CacheManagingException if Redis or deserialization operations fail.
     */
    @Override
    public <T> CompletableFuture<Optional<T>> get(String key, Class<T> clazz) {
        try {
            return commands.get(key)
                    .toCompletableFuture()
                    .thenApply(value -> {
                        if (value == null) {
                            log.debug("Cache miss: key={}", key);
                            return Optional.empty();
                        }
                        try {
                            T t = objectMapper.readValue(value, clazz);
                            log.debug("Cache hit: key={}", key);
                            return Optional.of(t);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cache value for key={}, returning empty", key);
                            return Optional.empty();
                        }
                    });
        } catch (Exception e) {
            log.error("Cache get failed: key={}", key, e);
            return CompletableFuture.failedFuture(new CacheManagingException(e));
        }
    }

    /**
     * Asynchronously stores a serialized object in Redis cache with the configured TTL.
     *
     * @param key Cache key.
     * @param value Object to cache.
     * @return A {@link CompletableFuture} completed when the operation finishes.
     * @throws CacheManagingException if serialization or Redis communication fails.
     */
    @Override
    public CompletableFuture<Void> put(String key, Object value) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            if (ttl.isPositive()) {
                long ttlInSeconds = ttl.toSeconds();
                return commands.setex(key, ttlInSeconds, jsonValue)
                        .toCompletableFuture()
                        .thenApply(v -> {
                            log.debug("Cache setex: key={}, ttl={}s", key, ttlInSeconds);
                            return null;
                        });
            } else {
                return commands.set(key, jsonValue)
                        .toCompletableFuture()
                        .thenApply(v -> {
                            log.debug("Cache set: key={}", key);
                            return null;
                        });
            }
        } catch (Exception e) {
            log.error("Cache put failed: key={}", key, e);
            return CompletableFuture.failedFuture(new CacheManagingException(e));
        }
    }

    /**
     * Closes the Redis connection and gracefully shuts down the client.
     *
     * @throws CacheManagingException if closing or shutdown fails.
     */
    @Override
    public void close() {
        try {
            log.info("Closing Redis cache connection");
            this.connection.close();
        } catch (Exception e) {
            log.error("Failed to close Redis connection", e);
            throw new CacheManagingException(e);
        } finally {
            this.redisClient.shutdown();
            log.info("Redis client shutdown issued");
        }
    }
}
