package io.maksymuimanov.task.exception;

public class CacheManagingException extends RuntimeException {
    public CacheManagingException() {
    }

    public CacheManagingException(String message) {
        super(message);
    }

    public CacheManagingException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheManagingException(Throwable cause) {
        super(cause);
    }

    public CacheManagingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
