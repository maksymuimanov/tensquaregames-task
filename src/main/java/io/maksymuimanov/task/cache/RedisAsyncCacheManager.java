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

@Slf4j
public class RedisAsyncCacheManager implements AsyncCacheManager {
    public static final String DEFAULT_REDIS_URL = "redis://localhost:6379";
    public static final Duration DEFAULT_TTL = Duration.ofMinutes(5);
    public static final String REDIS_URL_PROPERTY = "redis.url";
    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;
    private final RedisAsyncCommands<String, String> commands;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public RedisAsyncCacheManager(ObjectMapper objectMapper) {
        this(System.getProperty(REDIS_URL_PROPERTY, DEFAULT_REDIS_URL), objectMapper);
    }

    public RedisAsyncCacheManager(String url,
                                  ObjectMapper objectMapper) {
        this(url, objectMapper, DEFAULT_TTL);
    }

    public RedisAsyncCacheManager(String url,
                                  ObjectMapper objectMapper,
                                  Duration ttl) {
        this(RedisClient.create(url), objectMapper, ttl);
    }

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
            throw new CacheManagingException(e);
        }
    }

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
