package com.github.nginate.kafka.exceptions;

public class ApiException extends KafkaException {
    private static final long serialVersionUID = 2342116151549303556L;

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
