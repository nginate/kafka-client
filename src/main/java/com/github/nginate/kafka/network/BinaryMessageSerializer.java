package com.github.nginate.kafka.network;

import com.github.nginate.kafka.exceptions.SerializationException;
import com.github.nginate.kafka.network.client.BinaryClientContext;
import io.netty.buffer.ByteBuf;

public interface BinaryMessageSerializer {
    void serialize(ByteBuf buf, Object message) throws SerializationException;

    Object deserialize(ByteBuf buf, BinaryClientContext clientContext) throws SerializationException;
}
