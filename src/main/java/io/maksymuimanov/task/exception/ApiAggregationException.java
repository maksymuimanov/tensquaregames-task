package io.maksymuimanov.task.exception;

public class ApiAggregationException extends RuntimeException {
    public ApiAggregationException() {
    }

    public ApiAggregationException(String message) {
        super(message);
    }

    public ApiAggregationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiAggregationException(Throwable cause) {
        super(cause);
    }

    public ApiAggregationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
