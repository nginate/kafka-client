package com.github.nginate.kafka.exceptions;

public abstract class AbstractServerException extends KafkaException {
    public AbstractServerException(String message) {
        super(message);
    }

    public AbstractServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
