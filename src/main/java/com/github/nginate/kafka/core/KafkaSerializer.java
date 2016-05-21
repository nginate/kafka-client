package com.github.nginate.kafka.core;

@FunctionalInterface
public interface KafkaSerializer {
    byte[] serialize(Object payload);
}
