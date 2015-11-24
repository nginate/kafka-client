package com.github.nginate.kafka.exceptions;

public class ConnectionException extends CommunicationException {
    private static final long serialVersionUID = 3837216158334431903L;

    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
