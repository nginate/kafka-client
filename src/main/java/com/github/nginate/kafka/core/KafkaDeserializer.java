package com.github.nginate.kafka.core;

@FunctionalInterface
public interface KafkaDeserializer<T> {
    T deserialize(byte[] rawData);
}
