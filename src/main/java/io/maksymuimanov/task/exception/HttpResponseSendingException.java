package io.maksymuimanov.task.exception;

public class HttpResponseSendingException extends RuntimeException {
    public HttpResponseSendingException() {
    }

    public HttpResponseSendingException(String message) {
        super(message);
    }

    public HttpResponseSendingException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpResponseSendingException(Throwable cause) {
        super(cause);
    }

    public HttpResponseSendingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
