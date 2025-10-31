package io.maksymuimanov.task.dto;

import io.netty.handler.codec.http.HttpMethod;

public record HttpEndpoint(String path, HttpMethod method) {
}
