package io.maksymuimanov.task.application;

import com.redis.testcontainers.RedisContainer;
import io.maksymuimanov.task.cache.RedisAsyncCacheManager;
import io.maksymuimanov.task.endpoint.DashboardGetAsyncHttpEndpointProcessor;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Timeout;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class SimpleNettyApplicationTests {
    static RedisContainer redisContainer = new RedisContainer("redis:7-alpine");
    static SimpleNettyApplication application = new SimpleNettyApplication();
    static ExecutorService appExecutor;

    @BeforeAll
    static void prepareBeforeTests() {
        redisContainer.start();
        System.setProperty(RedisAsyncCacheManager.REDIS_URL_PROPERTY, redisContainer.getRedisURI());
        application = new SimpleNettyApplication();
        appExecutor = Executors.newSingleThreadExecutor();
        appExecutor.submit(application::run);

        try {
            Thread.sleep(Duration.ofSeconds(5).toMillis());
        } catch (InterruptedException ignored) {
        }

        RestAssured.baseURI = "http://localhost:8080";
    }

    @AfterAll
    static void cleanupAfterTests() {
        redisContainer.stop();
        appExecutor.shutdownNow();
        appExecutor.close();
    }

    @RepeatedTest(25)
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void shouldGetDashboardSuccessfully() {
        RestAssured.get(DashboardGetAsyncHttpEndpointProcessor.DASHBOARD_ENDPOINT_PATH)
                .then()
                .statusCode(200)
                .body("weather", Matchers.notNullValue())
                .body("fact", Matchers.notNullValue())
                .body("ip", Matchers.notNullValue());
    }
}
