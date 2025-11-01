package io.maksymuimanov.task.application;

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
public class SimpleNettyApplication implements NettyApplication {
    @Override
    public void run() {
        log.info("Starting Simple Netty-based application...");
        log.info("Initializing application components...");
        log.debug("Initializing ObjectMapper");
        ObjectMapper objectMapper = new ObjectMapper();
        log.debug("Initializing RedisAsyncCacheManager");
        AsyncCacheManager cacheManager = new RedisAsyncCacheManager(objectMapper);
        log.debug("Initializing RetryableAsyncApiRequestSender");
        AsyncApiRequestSender<String> apiRequestSender = new RetryableAsyncApiRequestSender();
        log.debug("Initializing JsonAsyncApiFetcher");
        AsyncApiFetcher<JsonNode> jsonApiFetcher = new JsonAsyncApiFetcher(objectMapper, apiRequestSender);
        log.debug("Initializing DashboardAsyncApiAggregator");
        AsyncApiAggregator<DashboardResponse> dashboardApiAggregator = new DashboardAsyncApiAggregator(jsonApiFetcher);
        log.debug("Initializing JsonHttpResponseSender");
        HttpResponseSender responseSender = new JsonHttpResponseSender(objectMapper);
        log.debug("Initializing DashboardGetAsyncHttpEndpointProcessor");
        AsyncHttpEndpointProcessor dashboardGetEndpointProcessor = new DashboardGetAsyncHttpEndpointProcessor(cacheManager, dashboardApiAggregator);
        log.debug("Initializing endpoint handlers map");
        Map<HttpEndpoint, AsyncHttpEndpointProcessor> endpointHandlers = Map.of(dashboardGetEndpointProcessor.getEndpoint(), dashboardGetEndpointProcessor);
        log.debug("Initializing SimpleHttpEndpointDirector");
        HttpEndpointDirector endpointDirector = new SimpleHttpEndpointDirector(endpointHandlers);
        log.debug("Initializing ServerEndpointChannelInboundHandler");
        ServerEndpointChannelInboundHandler serverEndpointHandler = new ServerEndpointChannelInboundHandler(responseSender, endpointDirector);
        log.debug("Initializing SimpleNettyServer");
        NettyServer nettyServer = new SimpleNettyServer(serverEndpointHandler);

        this.addShutdownHook(() -> {
            try {
                log.warn("Gracefully shutting down cache...");
                cacheManager.close();
            } catch (Exception ignored) {
                log.warn("Failed to gracefully shut down cache");
            }
        });
        this.addShutdownHook(() -> {
            try {
                log.warn("Gracefully shutting down default HTTP executor...");
                JsonAsyncApiFetcher.DEFAULT_HTTP_EXECUTOR.shutdown();
            } catch (Exception ignored) {
                log.warn("Failed to gracefully shut down default HTTP executor");
            }
        });

        nettyServer.run();
    }

    private void addShutdownHook(Runnable runnable) {
        Runtime runtime = Runtime.getRuntime();
        Thread hook = new Thread(runnable);
        runtime.addShutdownHook(hook);
    }
}
