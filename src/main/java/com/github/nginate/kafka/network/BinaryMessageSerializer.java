package com.github.nginate.kafka.network;

import com.github.nginate.kafka.exceptions.SerializationException;
import io.netty.buffer.ByteBuf;

public interface BinaryMessageSerializer {
    void serialize(ByteBuf buf, Object message) throws SerializationException;

    Object deserialize(ByteBuf buf) throws SerializationException;
}
