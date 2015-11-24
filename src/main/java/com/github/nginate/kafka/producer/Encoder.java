package com.github.nginate.kafka.producer;

public interface Encoder {
    byte[] encode(Object data);
}
