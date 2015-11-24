package com.github.nginate.kafka.exceptions;

public class KafkaTimeoutException extends KafkaException {
    private static final long serialVersionUID = -5319215478036589404L;

    public KafkaTimeoutException(String message) {
        super(message);
    }

    public KafkaTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
