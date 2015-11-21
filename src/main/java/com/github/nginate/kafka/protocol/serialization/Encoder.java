package com.github.nginate.kafka.protocol.serialization;

public interface Encoder {
    byte[] encode(Object data);
}
