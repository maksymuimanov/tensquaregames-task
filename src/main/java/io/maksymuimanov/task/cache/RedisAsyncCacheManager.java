package io.maksymuimanov.task.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.maksymuimanov.task.exception.CacheManagingException;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RedisAsyncCacheManager implements AsyncCacheManager {
    public static final String DEFAULT_REDIS_URL = "redis://localhost:6379";
    public static final Duration DEFAULT_TTL = Duration.ofMinutes(5);
    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;
    private final RedisAsyncCommands<String, String> commands;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public RedisAsyncCacheManager(ObjectMapper objectMapper) {
        this(DEFAULT_REDIS_URL, objectMapper);
    }

    public RedisAsyncCacheManager(String url, ObjectMapper objectMapper) {
        this(url, objectMapper, DEFAULT_TTL);
    }

    public RedisAsyncCacheManager(String url, ObjectMapper objectMapper, Duration ttl) {
        this(RedisClient.create(url), objectMapper, ttl);
    }

    public RedisAsyncCacheManager(RedisClient redisClient, ObjectMapper objectMapper, Duration ttl) {
        this.redisClient = redisClient;
        this.connection = redisClient.connect();
        this.commands = this.connection.async();
        this.objectMapper = objectMapper;
        this.ttl = ttl;
    }

    @Override
    public <T> CompletableFuture<Optional<T>> get(String key, Class<T> clazz) {
        try {
            return commands.get(key)
                    .toCompletableFuture()
                    .thenApply(value -> {
                        if (value == null) return Optional.empty();
                        try {
                            T t = objectMapper.readValue(value, clazz);
                            return Optional.ofNullable(t);
                        } catch (Exception e) {
                            return Optional.empty();
                        }
                    });
        } catch (Exception e) {
            throw new CacheManagingException(e);
        }
    }

    @Override
    public CompletableFuture<Void> put(String key, Object value) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            if (ttl.isPositive()) {
                return commands.setex(key, ttl.toSeconds(), jsonValue)
                        .toCompletableFuture()
                        .thenApply(v -> null);
            } else {
                return commands.set(key, jsonValue)
                        .toCompletableFuture()
                        .thenApply(v -> null);
            }
        } catch (Exception e) {
            throw new CacheManagingException(e);
        }
    }

    @Override
    public void close() {
        try {
            this.connection.close();
        } catch (Exception e) {
            throw new CacheManagingException(e);
        } finally {
            this.redisClient.shutdown();
        }
    }
}
