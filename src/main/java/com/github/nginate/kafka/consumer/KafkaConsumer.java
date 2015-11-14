package com.github.nginate.kafka.consumer;

public interface KafkaConsumer<T> {
    void poll();

    void addMessageHandler(MessageHandler<T> messageHandler);

    void removeMessageHandler(MessageHandler<T> messageHandler);
}
