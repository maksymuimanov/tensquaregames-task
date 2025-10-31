package io.maksymuimanov.task.api;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public interface AsyncApiRequestSender<T> {
    CompletableFuture<HttpResponse<T>> send(HttpClient httpClient, HttpRequest request, HttpResponse.BodyHandler<T> handler);
}
