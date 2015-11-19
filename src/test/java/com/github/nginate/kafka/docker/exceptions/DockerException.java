package com.github.nginate.kafka.docker.exceptions;

public class DockerException extends RuntimeException {
    private static final long serialVersionUID = -3730944990780364099L;

    public DockerException(String message) {
        super(message);
    }

    public DockerException(String message, Throwable cause) {
        super(message, cause);
    }

    public DockerException(Throwable cause) {
        super(cause);
    }
}
