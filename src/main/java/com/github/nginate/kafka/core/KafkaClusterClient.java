package com.github.nginate.kafka.core;


public interface KafkaClusterClient {
    <T> void subscribeWith(String topic, KafkaDeserializer<T> deserializer, MessageHandler<T> messageHandler);

    void unSubscribeFrom(String topic);

    void send(String topic, Object message);
}
