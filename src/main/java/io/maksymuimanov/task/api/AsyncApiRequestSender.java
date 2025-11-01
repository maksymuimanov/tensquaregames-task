package io.maksymuimanov.task.api;

import org.jspecify.annotations.NonNull;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public interface AsyncApiRequestSender<T> {
    @NonNull
    CompletableFuture<HttpResponse<T>> send(@NonNull HttpClient httpClient, @NonNull HttpRequest request, HttpResponse.@NonNull BodyHandler<T> handler);
}
