package io.maksymuimanov.task.cache;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RedisAsyncCacheManager implements AsyncCacheManager {
    public static final String DEFAULT_REDIS_URL = "redis://localhost:6379";
    private final StatefulRedisConnection<String, String> connection;
    private final RedisAsyncCommands<String, String> commands;

    public RedisAsyncCacheManager() {
        this(DEFAULT_REDIS_URL);
    }

    public RedisAsyncCacheManager(String url) {
        this(RedisClient.create(url));
    }

    public RedisAsyncCacheManager(RedisClient redisClient) {
        this.connection = redisClient.connect();
        this.commands = this.connection.async();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<Optional<T>> get(String key, Class<T> clazz) {
        return commands.get(key)
                .toCompletableFuture()
                .thenApply(value -> Optional.ofNullable((T) value));
    }

    @Override
    public CompletableFuture<Void> put(String key, Object value) {
        return commands.set(key, value.toString())
                .toCompletableFuture()
                .thenApply(v -> null);
    }

    @Override
    public CompletableFuture<Void> remove(String key) {
        return commands.del(key)
                .toCompletableFuture()
                .thenApply(v -> null);
    }

    @Override
    public void close() {
        this.connection.close();
    }
}
