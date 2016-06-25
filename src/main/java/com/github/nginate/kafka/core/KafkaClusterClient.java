package com.github.nginate.kafka.core;


import java.util.concurrent.CompletableFuture;

public interface KafkaClusterClient {

    boolean isClusterOperational();

    <T> void subscribeWith(String topic, KafkaDeserializer<T> deserializer, MessageHandler<T> messageHandler);

    void unSubscribeFrom(String topic);

    CompletableFuture<Void> send(String topic, Object message);

    void close();
}
