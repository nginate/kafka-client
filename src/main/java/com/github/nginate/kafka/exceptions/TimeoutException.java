package com.github.nginate.kafka.exceptions;

public class TimeoutException extends KafkaException {
    private static final long serialVersionUID = -5319215478036589404L;

    public TimeoutException(String message) {
        super(message);
    }

    public TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
