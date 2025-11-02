package cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.maksymuimanov.task.cache.AsyncCacheManager;
import io.maksymuimanov.task.cache.RedisAsyncCacheManager;
import io.maksymuimanov.task.exception.CacheManagingException;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unchecked")
class RedisAsyncCacheManagerTests {
    static final String TEST_STRING = "test";
    AsyncCacheManager redisAsyncCacheManager;
    RedisClient redisClient;
    StatefulRedisConnection<String, String> connection;
    RedisAsyncCommands<String, String> commands;
    ObjectMapper objectMapper;
    Duration ttl;
    RedisFuture<String> stringRedisFuture;

    @BeforeEach
    void setUp() {
        redisClient = Mockito.mock(RedisClient.class);
        connection = Mockito.mock(StatefulRedisConnection.class);
        commands = Mockito.mock(RedisAsyncCommands.class);
        objectMapper = Mockito.mock(ObjectMapper.class);
        ttl = Mockito.mock(Duration.class);
        stringRedisFuture = Mockito.mock(RedisFuture.class);
        Mockito.when(redisClient.connect()).thenReturn(connection);
        Mockito.when(connection.async()).thenReturn(commands);
        redisAsyncCacheManager = new RedisAsyncCacheManager(redisClient, objectMapper, ttl);
    }

    @Test
    void shouldGetNonNullSuccessfully() throws JsonProcessingException {
        CompletableFuture<String> stringCompletableFuture = CompletableFuture.completedFuture(TEST_STRING);

        Mockito.when(commands.get(TEST_STRING)).thenReturn(stringRedisFuture);
        Mockito.when(stringRedisFuture.toCompletableFuture()).thenReturn(stringCompletableFuture);
        Mockito.when(objectMapper.readValue(TEST_STRING, String.class)).thenReturn(TEST_STRING);

        CompletableFuture<Optional<String>> result = redisAsyncCacheManager.get(TEST_STRING, String.class);
        Awaitility.await().untilAsserted(result::isDone);
        Mockito.verify(commands).get(TEST_STRING);
        Mockito.verify(stringRedisFuture).toCompletableFuture();
        Mockito.verify(objectMapper).readValue(TEST_STRING, String.class);
        Assertions.assertEquals(Optional.of(TEST_STRING), result.join());
    }

    @Test
    void shouldGetNullSuccessfully() throws JsonProcessingException {
        CompletableFuture<String> stringCompletableFuture = CompletableFuture.completedFuture(null);

        Mockito.when(commands.get(TEST_STRING)).thenReturn(stringRedisFuture);
        Mockito.when(stringRedisFuture.toCompletableFuture()).thenReturn(stringCompletableFuture);

        CompletableFuture<Optional<String>> result = redisAsyncCacheManager.get(TEST_STRING, String.class);
        Awaitility.await().untilAsserted(result::isDone);
        Mockito.verify(commands).get(TEST_STRING);
        Mockito.verify(stringRedisFuture).toCompletableFuture();
        Mockito.verify(objectMapper, Mockito.never()).readValue((String) null, String.class);
        Assertions.assertEquals(Optional.empty(), result.join());
    }

    @Test
    void shouldGetNullOnExceptionSuccessfully() throws JsonProcessingException {
        CompletableFuture<String> stringCompletableFuture = CompletableFuture.completedFuture(TEST_STRING);

        Mockito.when(commands.get(TEST_STRING)).thenReturn(stringRedisFuture);
        Mockito.when(stringRedisFuture.toCompletableFuture()).thenReturn(stringCompletableFuture);
        Mockito.when(objectMapper.readValue(TEST_STRING, String.class)).thenThrow(RuntimeException.class);

        CompletableFuture<Optional<String>> result = redisAsyncCacheManager.get(TEST_STRING, String.class);
        Awaitility.await().untilAsserted(result::isDone);
        Mockito.verify(commands).get(TEST_STRING);
        Mockito.verify(stringRedisFuture).toCompletableFuture();
        Mockito.verify(objectMapper).readValue(TEST_STRING, String.class);
        Assertions.assertEquals(Optional.empty(), result.join());
    }

    @Test
    void shouldFailToGet() {
        Mockito.when(commands.get(TEST_STRING)).thenThrow(RuntimeException.class);

        Assertions.assertThrows(CacheManagingException.class, () -> redisAsyncCacheManager.get(TEST_STRING, String.class));
    }

    @Test
    void shouldPutWithTtlPositiveSuccessfully() throws JsonProcessingException {
        CompletableFuture<String> stringCompletableFuture = CompletableFuture.completedFuture(TEST_STRING);

        Mockito.when(objectMapper.writeValueAsString(TEST_STRING)).thenReturn(TEST_STRING);
        Mockito.when(ttl.isPositive()).thenReturn(true);
        Mockito.when(commands.setex(TEST_STRING, ttl.toSeconds(), TEST_STRING)).thenReturn(stringRedisFuture);
        Mockito.when(stringRedisFuture.toCompletableFuture()).thenReturn(stringCompletableFuture);
        
        CompletableFuture<Void> result = redisAsyncCacheManager.put(TEST_STRING, TEST_STRING);
        Awaitility.await().untilAsserted(result::isDone);
        Mockito.verify(objectMapper).writeValueAsString(TEST_STRING);
        Mockito.verify(ttl).isPositive();
        Mockito.verify(commands).setex(TEST_STRING, ttl.toSeconds(), TEST_STRING);
        Mockito.verify(commands, Mockito.never()).set(TEST_STRING, TEST_STRING);
    }

    @Test
    void shouldPutWithTtlNonPositiveSuccessfully() throws JsonProcessingException {
        CompletableFuture<String> stringCompletableFuture = CompletableFuture.completedFuture(TEST_STRING);

        Mockito.when(objectMapper.writeValueAsString(TEST_STRING)).thenReturn(TEST_STRING);
        Mockito.when(ttl.isPositive()).thenReturn(false);
        Mockito.when(commands.set(TEST_STRING, TEST_STRING)).thenReturn(stringRedisFuture);
        Mockito.when(stringRedisFuture.toCompletableFuture()).thenReturn(stringCompletableFuture);

        CompletableFuture<Void> result = redisAsyncCacheManager.put(TEST_STRING, TEST_STRING);
        Awaitility.await().untilAsserted(result::isDone);
        Mockito.verify(objectMapper).writeValueAsString(TEST_STRING);
        Mockito.verify(ttl).isPositive();
        Mockito.verify(commands).set(TEST_STRING, TEST_STRING);
        Mockito.verify(commands, Mockito.never()).setex(TEST_STRING, ttl.toSeconds(), TEST_STRING);
    }

    @Test
    void shouldFailToPutWithTtl() throws JsonProcessingException {
        Mockito.when(objectMapper.writeValueAsString(TEST_STRING)).thenThrow(RuntimeException.class);

        Assertions.assertThrows(CacheManagingException.class, () -> redisAsyncCacheManager.put(TEST_STRING, TEST_STRING));
    }

    @Test
    void shouldCloseSuccessfully() {
        Mockito.doNothing().when(connection).close();
        Mockito.doNothing().when(redisClient).shutdown();

        Assertions.assertDoesNotThrow(() -> redisAsyncCacheManager.close());
        Mockito.verify(connection).close();
        Mockito.verify(redisClient).shutdown();
    }

    @Test
    void shouldFailToClose() {
        Mockito.doThrow(RuntimeException.class).when(connection).close();
        Mockito.doNothing().when(redisClient).shutdown();

        Assertions.assertThrows(CacheManagingException.class, () -> redisAsyncCacheManager.close());
        Mockito.verify(connection).close();
        Mockito.verify(redisClient).shutdown();
    }
}