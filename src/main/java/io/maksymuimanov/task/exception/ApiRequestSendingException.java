package io.maksymuimanov.task.exception;

public class ApiRequestSendingException extends RuntimeException {
    public ApiRequestSendingException(String message) {
        super(message);
    }
}
