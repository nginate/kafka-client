package com.github.nginate.kafka;

public interface KafkaMessage<T> {
    long getTimestamp();

    void setTimestamp(long timestamp);

    T getPayload();

    void setPayload(T payload);
}
