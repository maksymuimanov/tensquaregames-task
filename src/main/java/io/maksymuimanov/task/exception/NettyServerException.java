package io.maksymuimanov.task.exception;

public class NettyServerException extends RuntimeException {
    public NettyServerException() {
    }

    public NettyServerException(String message) {
        super(message);
    }

    public NettyServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public NettyServerException(Throwable cause) {
        super(cause);
    }

    public NettyServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
