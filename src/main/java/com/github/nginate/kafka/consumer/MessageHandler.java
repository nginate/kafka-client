package com.github.nginate.kafka.consumer;

import com.github.nginate.kafka.KafkaMessage;

@FunctionalInterface
public interface MessageHandler<T> {
    void onMessage(KafkaMessage<T> message);
}
