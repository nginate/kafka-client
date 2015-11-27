package com.github.nginate.kafka.protocol;

import com.github.nginate.kafka.exceptions.SerializationException;
import com.github.nginate.kafka.network.BinaryMessageSerializer;
import com.github.nginate.kafka.protocol.messages.Request;
import io.netty.buffer.ByteBuf;

public class KafkaSerializer implements BinaryMessageSerializer {
    @Override
    public void serialize(ByteBuf buf, Object message) throws SerializationException {
        if (message instanceof Request) {
            Request request = (Request) message;
        }
        throw new SerializationException("Unsupported message type");
    }

    @Override
    public Object deserialize(ByteBuf buf) throws SerializationException {
        return null;
    }
}
