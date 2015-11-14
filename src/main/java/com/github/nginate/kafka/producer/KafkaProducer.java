package com.github.nginate.kafka.producer;

public interface KafkaProducer {

    void send(Object message);

    void start();

    void stop();
}
