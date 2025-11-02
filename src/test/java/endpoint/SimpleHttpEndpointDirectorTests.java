package endpoint;

import io.maksymuimanov.task.dto.HttpEndpoint;
import io.maksymuimanov.task.endpoint.*;
import io.maksymuimanov.task.exception.HttpEndpointDirectingException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unchecked")
class SimpleHttpEndpointDirectorTests {
    static final DefaultFullHttpRequest TEST_HTTP_REQUEST = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, DashboardGetAsyncHttpEndpointProcessor.DASHBOARD_HTTP_ENDPOINT.method(), DashboardGetAsyncHttpEndpointProcessor.DASHBOARD_HTTP_ENDPOINT.path());
    HttpEndpointDirector endpointDirector;
    Map<HttpEndpoint, AsyncHttpEndpointProcessor> endpointProcessors;
    ChannelHandlerContext context;
    HttpResponseSender responseSender;
    AsyncHttpEndpointProcessor endpointProcessor;

    @BeforeEach
    void setUp() {
        endpointProcessors = Mockito.mock(Map.class);
        context = Mockito.mock(ChannelHandlerContext.class);
        responseSender = Mockito.mock(HttpResponseSender.class);
        endpointProcessor = Mockito.mock(AsyncHttpEndpointProcessor.class);
        endpointDirector = new SimpleHttpEndpointDirector(endpointProcessors);
    }

    @Test
    void shouldDirectSuccessfully() {
        CompletableFuture<Void> voidFuture = CompletableFuture.completedFuture(null);

        Mockito.when(endpointProcessors.containsKey(DashboardGetAsyncHttpEndpointProcessor.DASHBOARD_HTTP_ENDPOINT)).thenReturn(true);
        Mockito.when(endpointProcessors.get(DashboardGetAsyncHttpEndpointProcessor.DASHBOARD_HTTP_ENDPOINT)).thenReturn(endpointProcessor);
        Mockito.when(endpointProcessor.process(context, responseSender, true)).thenReturn(voidFuture);

        Assertions.assertDoesNotThrow(() -> endpointDirector.direct(context, TEST_HTTP_REQUEST, responseSender));
    }

    @Test
    void shouldDirectWithUnexpectedServerError() {
        CompletableFuture<Void> failedFuture = CompletableFuture.failedFuture(new RuntimeException("Test exception"));
        Channel channel = Mockito.mock(Channel.class);

        Mockito.when(endpointProcessors.containsKey(DashboardGetAsyncHttpEndpointProcessor.DASHBOARD_HTTP_ENDPOINT)).thenReturn(true);
        Mockito.when(endpointProcessors.get(DashboardGetAsyncHttpEndpointProcessor.DASHBOARD_HTTP_ENDPOINT)).thenReturn(endpointProcessor);
        Mockito.when(endpointProcessor.process(context, responseSender, true)).thenReturn(failedFuture);
        Mockito.when(context.channel()).thenReturn(channel);
        Mockito.when(channel.isActive()).thenReturn(true);

        Assertions.assertDoesNotThrow(() -> endpointDirector.direct(context, TEST_HTTP_REQUEST, responseSender));
        Mockito.verify(responseSender).send(context, DashboardGetAsyncHttpEndpointProcessor.UNEXPECTED_SERVER_ERROR_MESSAGE, HttpResponseStatus.INTERNAL_SERVER_ERROR, true);
    }

    @Test
    void shouldDirectWithNotFound() {
        Mockito.when(endpointProcessors.containsKey(DashboardGetAsyncHttpEndpointProcessor.DASHBOARD_HTTP_ENDPOINT)).thenReturn(false);

        Assertions.assertDoesNotThrow(() -> endpointDirector.direct(context, TEST_HTTP_REQUEST, responseSender));
        Mockito.verify(responseSender).send(context, SimpleHttpEndpointDirector.NOT_FOUND_MESSAGE, HttpResponseStatus.NOT_FOUND, true);
    }

    @Test
    void shouldFailToDirect() {
        Mockito.when(endpointProcessors.containsKey(DashboardGetAsyncHttpEndpointProcessor.DASHBOARD_HTTP_ENDPOINT)).thenThrow(RuntimeException.class);

        Assertions.assertThrows(HttpEndpointDirectingException.class, () -> endpointDirector.direct(context, TEST_HTTP_REQUEST, responseSender));
    }
}
