package io.maksymuimanov.task.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@SuppressWarnings("unchecked")
class JsonAsyncApiFetcherTests {
    static final String TEST_URL = "http://localhost:8080/";
    static final URI TEST_URI = URI.create(TEST_URL);
    static final HttpRequest TEST_HTTP_REQUEST = HttpRequest.newBuilder(TEST_URI)
            .GET()
            .timeout(JsonAsyncApiFetcher.DEFAULT_REQUEST_TIMEOUT)
            .build();
    static final HttpResponse.BodyHandler<String> TEST_RESPONSE_BODY_HANDLER = HttpResponse.BodyHandlers.ofString();
    static final String JSON_RESPONSE_BODY = "{\"test\":\"test\"}";
    AsyncApiFetcher<JsonNode> apiFetcher;
    HttpClient httpClient;
    ObjectMapper objectMapper;
    AsyncApiRequestSender<String> requestSender;
    HttpResponse<String> response;
    JsonNode jsonNode;

    @BeforeEach
    void setUp() {
        httpClient = Mockito.mock(HttpClient.class);
        objectMapper = Mockito.mock(ObjectMapper.class);
        requestSender = Mockito.mock(AsyncApiRequestSender.class);
        response = Mockito.mock(HttpResponse.class);
        jsonNode = Mockito.mock(JsonNode.class);
        apiFetcher = new JsonAsyncApiFetcher(httpClient, objectMapper, requestSender);
    }

    @Test
    void shouldFetchSuccessfully() throws JsonProcessingException {
        Mockito.when(requestSender.send(httpClient, TEST_HTTP_REQUEST, TEST_RESPONSE_BODY_HANDLER)).thenReturn(CompletableFuture.completedFuture(response));
        Mockito.when(response.body()).thenReturn(JSON_RESPONSE_BODY);
        Mockito.when(objectMapper.readTree(JSON_RESPONSE_BODY)).thenReturn(jsonNode);

        CompletableFuture<JsonNode> result = apiFetcher.fetch(TEST_URL);
        Awaitility.await().untilAsserted(result::isDone);
        Mockito.verify(requestSender).send(httpClient, TEST_HTTP_REQUEST, TEST_RESPONSE_BODY_HANDLER);
        Mockito.verify(response).body();
        Mockito.verify(objectMapper).readTree(JSON_RESPONSE_BODY);
        Assertions.assertEquals(jsonNode, result.join());
    }

    @Test
    void shouldFailToFetch() throws JsonProcessingException {
        Mockito.when(requestSender.send(httpClient, TEST_HTTP_REQUEST, TEST_RESPONSE_BODY_HANDLER)).thenReturn(CompletableFuture.completedFuture(response));
        Mockito.when(response.body()).thenReturn(JSON_RESPONSE_BODY);
        Mockito.when(objectMapper.readTree(JSON_RESPONSE_BODY)).thenThrow(JsonProcessingException.class);

        CompletableFuture<JsonNode> result = apiFetcher.fetch(TEST_URL);
        Mockito.verify(requestSender).send(httpClient, TEST_HTTP_REQUEST, TEST_RESPONSE_BODY_HANDLER);
        Mockito.verify(response).body();
        Mockito.verify(objectMapper).readTree(JSON_RESPONSE_BODY);
        Awaitility.await().untilAsserted(result::isCompletedExceptionally);
        Assertions.assertThrows(CompletionException.class, result::join);
    }
}
