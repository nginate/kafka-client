package com.github.nginate.kafka.protocol.serialization;

public interface Decoder<T> {
    T decode(byte[] data);
}
