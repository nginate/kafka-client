package com.github.nginate.kafka.protocol.validation;

import com.github.nginate.kafka.exceptions.KafkaException;

@FunctionalInterface
public interface ResponseValidator<T> {
    void validate(T response) throws KafkaException;
}
