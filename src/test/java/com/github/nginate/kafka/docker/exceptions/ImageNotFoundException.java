package com.github.nginate.kafka.docker.exceptions;

public class ImageNotFoundException extends DockerException {
    public ImageNotFoundException(Throwable cause) {
        super(cause);
    }

    public ImageNotFoundException(String message) {
        super(message);
    }

    public ImageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
