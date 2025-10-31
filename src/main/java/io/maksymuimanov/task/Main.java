package io.maksymuimanov.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.maksymuimanov.task.api.AsyncApiAggregator;
import io.maksymuimanov.task.api.AsyncApiFetcher;
import io.maksymuimanov.task.api.DashboardAsyncApiAggregator;
import io.maksymuimanov.task.api.JsonAsyncApiFetcher;
import io.maksymuimanov.task.cache.AsyncCacheManager;
import io.maksymuimanov.task.cache.RedisAsyncCacheManager;
import io.maksymuimanov.task.dto.DashboardResponse;
import io.maksymuimanov.task.endpoint.DashboardEndpointHandler;
import io.maksymuimanov.task.server.NettyServer;
import io.maksymuimanov.task.server.SimpleNettyServer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        AsyncCacheManager cacheManager = new RedisAsyncCacheManager();
        ObjectMapper objectMapper = new ObjectMapper();
        AsyncApiFetcher<JsonNode> jsonApiFetcher = new JsonAsyncApiFetcher(objectMapper);
        AsyncApiAggregator<DashboardResponse> dashboardApiAggregator = new DashboardAsyncApiAggregator(jsonApiFetcher);
        DashboardEndpointHandler dashboardEndpointHandler = new DashboardEndpointHandler(dashboardApiAggregator, objectMapper, cacheManager);
        List<SimpleChannelInboundHandler<FullHttpRequest>> endpointHandlers = List.of(dashboardEndpointHandler);
        NettyServer nettyServer = new SimpleNettyServer(endpointHandlers, cacheManager);
        nettyServer.run();
    }
}