package com.github.nginate.kafka.zookeeper.exception;

import com.github.nginate.kafka.exceptions.KafkaException;

public class ZookeeperValidationException extends KafkaException {
    public ZookeeperValidationException(String message) {
        super(message);
    }

    public ZookeeperValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
