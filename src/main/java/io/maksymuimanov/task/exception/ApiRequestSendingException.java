package io.maksymuimanov.task.exception;

public class ApiRequestSendingException extends RuntimeException {
    public ApiRequestSendingException() {
    }

    public ApiRequestSendingException(String message) {
        super(message);
    }

    public ApiRequestSendingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiRequestSendingException(Throwable cause) {
        super(cause);
    }

    public ApiRequestSendingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
