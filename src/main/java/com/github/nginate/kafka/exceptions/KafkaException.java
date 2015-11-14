package com.github.nginate.kafka.exceptions;

public class KafkaException extends RuntimeException {
    private static final long serialVersionUID = -9198274319918051416L;

    public KafkaException(String message) {
        super(message);
    }

    public KafkaException(String message, Throwable cause) {
        super(message, cause);
    }
}
