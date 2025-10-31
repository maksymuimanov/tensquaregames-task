package io.maksymuimanov.task.exception;

public class ApiFetchingException extends RuntimeException {
    public ApiFetchingException() {
    }

    public ApiFetchingException(String message) {
        super(message);
    }

    public ApiFetchingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiFetchingException(Throwable cause) {
        super(cause);
    }

    public ApiFetchingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
