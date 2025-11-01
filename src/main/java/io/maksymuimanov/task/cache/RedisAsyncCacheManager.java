package io.maksymuimanov.task.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.maksymuimanov.task.exception.CacheManagingException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class RedisAsyncCacheManager implements AsyncCacheManager {
    public static final String DEFAULT_REDIS_URL = "redis://localhost:6379";
    public static final Duration DEFAULT_TTL = Duration.ofMinutes(5);
    @NonNull
    private final RedisClient redisClient;
    @NonNull
    private final StatefulRedisConnection<String, String> connection;
    @NonNull
    private final RedisAsyncCommands<String, String> commands;
    @NonNull
    private final ObjectMapper objectMapper;
    @NonNull
    private final Duration ttl;

    public RedisAsyncCacheManager(@NonNull ObjectMapper objectMapper) {
        this(DEFAULT_REDIS_URL, objectMapper);
    }

    public RedisAsyncCacheManager(@NonNull String url,
                                  @NonNull ObjectMapper objectMapper) {
        this(url, objectMapper, DEFAULT_TTL);
    }

    public RedisAsyncCacheManager(@NonNull String url,
                                  @NonNull ObjectMapper objectMapper,
                                  @NonNull Duration ttl) {
        this(RedisClient.create(url), objectMapper, ttl);
    }

    public RedisAsyncCacheManager(@NonNull RedisClient redisClient,
                                  @NonNull ObjectMapper objectMapper,
                                  @NonNull Duration ttl) {
        this.redisClient = redisClient;
        this.connection = redisClient.connect();
        this.commands = this.connection.async();
        this.objectMapper = objectMapper;
        this.ttl = ttl;
        log.info("Initialized Redis cache manager with TTL={}s", ttl.toSeconds());
    }

    @Override
    @NonNull
    public <T> CompletableFuture<Optional<T>> get(@NonNull String key, @NonNull Class<T> clazz) {
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
                            return Optional.ofNullable(t);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cache value for key={}, returning empty", key);
                            return Optional.empty();
                        }
                    });
        } catch (Exception e) {
            log.error("Cache get failed: key={}", key, e);
            throw new CacheManagingException(e);
        }
    }

    @Override
    @NonNull
    public CompletableFuture<Void> put(@NonNull String key, Object value) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            if (ttl.isPositive()) {
                return commands.setex(key, ttl.toSeconds(), jsonValue)
                        .toCompletableFuture()
                        .thenApply(v -> {
                            log.debug("Cache setex: key={}, ttl={}s", key, ttl.toSeconds());
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
            throw new CacheManagingException(e);
        }
    }

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
