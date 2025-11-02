package io.maksymuimanov.task.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.maksymuimanov.task.api.*;
import io.maksymuimanov.task.cache.AsyncCacheManager;
import io.maksymuimanov.task.cache.RedisAsyncCacheManager;
import io.maksymuimanov.task.dto.DashboardResponse;
import io.maksymuimanov.task.dto.HttpEndpoint;
import io.maksymuimanov.task.endpoint.*;
import io.maksymuimanov.task.server.HttpServerEndpointChannelInboundHandler;
import io.maksymuimanov.task.server.HttpSocketChannelInitializer;
import io.maksymuimanov.task.server.NettyServer;
import io.maksymuimanov.task.server.SimpleNettyServer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Boots the asynchronous Netty-based HTTP application by assembling all API,
 * caching, and endpoint-processing components. Configures the JSON mapper,
 * Redis cache, non-blocking API clients, and HTTP routing pipeline, then
 * starts a Netty server that exposes the /api/dashboard endpoint.
 * <p>
 * This class is responsible only for wiring components together and managing
 * application-level lifecycle (startup and shutdown hooks).
 *
 * @see NettyApplication
 * @see ObjectMapper
 * @see RedisAsyncCacheManager
 * @see RetryableAsyncApiRequestSender
 * @see JsonAsyncApiFetcher
 * @see DashboardAsyncApiAggregator
 * @see JsonHttpResponseSender
 * @see DashboardGetAsyncHttpEndpointProcessor
 * @see SimpleHttpEndpointDirector
 * @see HttpServerEndpointChannelInboundHandler
 * @see HttpSocketChannelInitializer
 * @see SimpleNettyServer
 */
@Slf4j
public class SimpleNettyApplication implements NettyApplication {
    /**
     * Initializes all infrastructure components (Redis cache manager,
     * async API fetcher, retryable HTTP sender, endpoint processors, Netty
     * handlers) and starts the Netty HTTP server in blocking mode.
     * <p>
     * This method performs only construction and wiring - all network and
     * API operations remain asynchronous at the processor/fetcher layer.
     */
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
        HttpServerEndpointChannelInboundHandler serverEndpointHandler = new HttpServerEndpointChannelInboundHandler(responseSender, endpointDirector);
        log.debug("Initializing HttpSocketChannelInitializer");
        ChannelInitializer<SocketChannel> channelInitializer = new HttpSocketChannelInitializer(serverEndpointHandler);
        log.debug("Initializing SimpleNettyServer");
        NettyServer nettyServer = new SimpleNettyServer(channelInitializer);

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

    /**
     * Registers a JVM shutdown hook that ensures proper cleanup of I/O-bound
     * components such as Redis connections and background executor threads.
     *
     * @param runnable cleanup logic to execute during JVM shutdown
     */
    private void addShutdownHook(Runnable runnable) {
        Runtime runtime = Runtime.getRuntime();
        Thread hook = new Thread(runnable);
        runtime.addShutdownHook(hook);
    }
}
