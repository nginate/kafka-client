package com.github.nginate.kafka.core;

public class KafkaSerializerImpl implements KafkaSerializer {
    @Override
    public byte[] serialize(Object payload) {
        return new byte[0];
    }
}
