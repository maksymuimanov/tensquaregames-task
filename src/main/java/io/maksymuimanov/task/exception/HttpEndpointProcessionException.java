package io.maksymuimanov.task.exception;

public class HttpEndpointProcessionException extends RuntimeException {
    public HttpEndpointProcessionException() {
    }

    public HttpEndpointProcessionException(String message) {
        super(message);
    }

    public HttpEndpointProcessionException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpEndpointProcessionException(Throwable cause) {
        super(cause);
    }

    public HttpEndpointProcessionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
