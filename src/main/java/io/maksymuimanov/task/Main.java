package io.maksymuimanov.task;

import io.maksymuimanov.task.application.NettyApplication;
import io.maksymuimanov.task.application.SimpleNettyApplication;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
    public static void main(String[] args) {
        NettyApplication application = new SimpleNettyApplication();
        application.run();
    }
}