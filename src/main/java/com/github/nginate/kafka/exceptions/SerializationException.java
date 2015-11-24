package com.github.nginate.kafka.exceptions;

public class SerializationException extends KafkaException {
    private static final long serialVersionUID = 150386498975596551L;

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
