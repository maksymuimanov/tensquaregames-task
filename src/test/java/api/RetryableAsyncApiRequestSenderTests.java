package api;

import io.maksymuimanov.task.api.AsyncApiRequestSender;
import io.maksymuimanov.task.api.RetryableAsyncApiRequestSender;
import io.maksymuimanov.task.exception.ApiRequestSendingException;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unchecked")
class RetryableAsyncApiRequestSenderTests {
    static final URI TEST_URI = URI.create("http://localhost:8080/");
    AsyncApiRequestSender<String> requestSender;
    HttpClient httpClient;
    HttpRequest request;
    HttpResponse.BodyHandler<String> handler;
    HttpResponse<String> response;

    @BeforeEach
    void setUp() {
        httpClient = Mockito.mock(HttpClient.class);
        request = Mockito.mock(HttpRequest.class);
        handler = Mockito.mock(HttpResponse.BodyHandler.class);
        response = Mockito.mock(HttpResponse.class);
        requestSender = new RetryableAsyncApiRequestSender(RetryableAsyncApiRequestSender.DEFAULT_RETRY_COUNT, Duration.ofMillis(50));
    }

    @Test
    void shouldSendSuccessfullyWithoutRetry() {
        CompletableFuture<HttpResponse<String>> futureResponse = CompletableFuture.completedFuture(response);

        Mockito.when(request.uri()).thenReturn(TEST_URI);
        Mockito.when(httpClient.sendAsync(request, handler)).thenReturn(futureResponse);
        Mockito.when(response.statusCode()).thenReturn(RetryableAsyncApiRequestSender.HTTP_OK_STATUS);

        CompletableFuture<HttpResponse<String>> result = requestSender.send(httpClient, request, handler);
        Awaitility.await().atMost(Duration.ofSeconds(1)).untilAsserted(result::isDone);
        Mockito.verify(httpClient, Mockito.times(1)).sendAsync(request, handler);
        Assertions.assertEquals(response, result.join());
    }

    @Test
    void shouldRetryOnceThenSucceed() {
        HttpResponse<String> badResponse = Mockito.mock(HttpResponse.class);
        CompletableFuture<HttpResponse<String>> badFutureResponse = CompletableFuture.completedFuture(badResponse);
        CompletableFuture<HttpResponse<String>> futureResponse = CompletableFuture.completedFuture(response);

        Mockito.when(request.uri()).thenReturn(TEST_URI);
        Mockito.when(httpClient.sendAsync(request, handler))
                .thenReturn(badFutureResponse)
                .thenReturn(futureResponse);
        Mockito.when(badResponse.statusCode()).thenReturn(RetryableAsyncApiRequestSender.HTTP_SUCCESS_CODE_LIMIT);
        Mockito.when(response.statusCode()).thenReturn(RetryableAsyncApiRequestSender.HTTP_OK_STATUS);

        CompletableFuture<HttpResponse<String>> result = requestSender.send(httpClient, request, handler);
        Awaitility.await().atMost(Duration.ofSeconds(1)).untilAsserted(result::isDone);
        Mockito.verify(httpClient, Mockito.times(2)).sendAsync(request, handler);
        Assertions.assertEquals(response, result.join());
    }

    @Test
    void shouldFailAfterAllRetries() {
        CompletableFuture<HttpResponse<String>> futureResponse = CompletableFuture.completedFuture(response);

        Mockito.when(request.uri()).thenReturn(TEST_URI);
        Mockito.when(httpClient.sendAsync(request, handler)).thenReturn(futureResponse);
        Mockito.when(response.statusCode()).thenReturn(RetryableAsyncApiRequestSender.HTTP_SUCCESS_CODE_LIMIT);

        CompletableFuture<HttpResponse<String>> result = requestSender.send(httpClient, request, handler);
        Awaitility.await().atMost(Duration.ofSeconds(1)).untilAsserted(() -> {
            Assertions.assertTrue(result.isDone());
            Mockito.verify(httpClient, Mockito.atLeast(3)).sendAsync(request, handler);
        });
        Mockito.verify(httpClient, Mockito.times(3)).sendAsync(request, handler);
    }

    @Test
    void shouldRetryOnExceptionThenFail() {
        CompletableFuture<HttpResponse<String>> futureResponse = CompletableFuture.failedFuture(new ApiRequestSendingException("Test exception"));

        Mockito.when(request.uri()).thenReturn(TEST_URI);
        Mockito.when(httpClient.sendAsync(request, handler)).thenReturn(futureResponse);

        CompletableFuture<HttpResponse<String>> result = requestSender.send(httpClient, request, handler);
        Awaitility.await().atMost(Duration.ofSeconds(1)).untilAsserted(() -> {
            Assertions.assertTrue(result.isCompletedExceptionally());
            Mockito.verify(httpClient, Mockito.atLeast(3)).sendAsync(request, handler);
        });
        Mockito.verify(httpClient, Mockito.times(3)).sendAsync(request, handler);
    }
}
