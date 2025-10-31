package io.maksymuimanov.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.maksymuimanov.task.api.*;
import io.maksymuimanov.task.cache.AsyncCacheManager;
import io.maksymuimanov.task.cache.RedisAsyncCacheManager;
import io.maksymuimanov.task.dto.DashboardResponse;
import io.maksymuimanov.task.dto.HttpEndpoint;
import io.maksymuimanov.task.endpoint.*;
import io.maksymuimanov.task.server.NettyServer;
import io.maksymuimanov.task.server.ServerEndpointChannelInboundHandler;
import io.maksymuimanov.task.server.SimpleNettyServer;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class Main {
    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        AsyncCacheManager cacheManager = new RedisAsyncCacheManager(objectMapper);
        AsyncApiRequestSender<String> apiRequestSender = new RetryableAsyncApiRequestSender();
        AsyncApiFetcher<JsonNode> jsonApiFetcher = new JsonAsyncApiFetcher(objectMapper, apiRequestSender);
        AsyncApiAggregator<DashboardResponse> dashboardApiAggregator = new DashboardAsyncApiAggregator(jsonApiFetcher);
        HttpResponseSender responseSender = new JsonHttpResponseSender(objectMapper);
        AsyncHttpEndpointProcessor dashboardGetEndpointProcessor = new DashboardGetAsyncHttpEndpointProcessor(cacheManager, dashboardApiAggregator);
        Map<HttpEndpoint, AsyncHttpEndpointProcessor> endpointHandlers = Map.of(dashboardGetEndpointProcessor.getEndpoint(), dashboardGetEndpointProcessor);
        HttpEndpointDirector endpointDirector = new SimpleHttpEndpointDirector(endpointHandlers);
        ServerEndpointChannelInboundHandler serverEndpointHandler = new ServerEndpointChannelInboundHandler(responseSender, endpointDirector);
        NettyServer nettyServer = new SimpleNettyServer(serverEndpointHandler);

        addShutdownHook(() -> {
            try {
                log.warn("Gracefully shutting down cache...");
                cacheManager.close();
            } catch (Exception ignored) {
                log.warn("Failed to gracefully shut down cache");
            }
        });
        addShutdownHook(() -> {
            try {
                log.warn("Gracefully shutting down default HTTP executor...");
                JsonAsyncApiFetcher.DEFAULT_HTTP_EXECUTOR.shutdown();
            } catch (Exception ignored) {
                log.warn("Failed to gracefully shut down default HTTP executor");
            }
        });

        nettyServer.run();
    }

    private static void addShutdownHook(Runnable runnable) {
        Runtime runtime = Runtime.getRuntime();
        Thread hook = new Thread(runnable);
        runtime.addShutdownHook(hook);
    }
}