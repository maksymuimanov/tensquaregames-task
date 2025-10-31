package io.maksymuimanov.task.exception;

public class HttpEndpointDirectingException extends RuntimeException {
    public HttpEndpointDirectingException() {
    }

    public HttpEndpointDirectingException(String message) {
        super(message);
    }

    public HttpEndpointDirectingException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpEndpointDirectingException(Throwable cause) {
        super(cause);
    }

    public HttpEndpointDirectingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
