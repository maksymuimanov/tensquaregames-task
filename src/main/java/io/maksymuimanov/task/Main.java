package io.maksymuimanov.task;

import io.maksymuimanov.task.server.NettyServer;
import io.maksymuimanov.task.server.SimpleNettyServer;

public class Main {
    public static void main(String[] args) {
        NettyServer nettyServer = new SimpleNettyServer();
        nettyServer.run();
    }
}