package com.github.nginate.kafka.consumer;

public interface Decoder<T> {
    T decode(byte[] data);
}
