package com.github.nginate.kafka.core;

@FunctionalInterface
public interface MessageHandler<T> {
    void onMessage(T message);
}
