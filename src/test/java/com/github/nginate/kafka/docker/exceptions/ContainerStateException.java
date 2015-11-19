package com.github.nginate.kafka.docker.exceptions;

public class ContainerStateException extends DockerException {
    private static final long serialVersionUID = 6742540677378763024L;

    public ContainerStateException(String message) {
        super(message);
    }

    public ContainerStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContainerStateException(Throwable cause) {
        super(cause);
    }
}
