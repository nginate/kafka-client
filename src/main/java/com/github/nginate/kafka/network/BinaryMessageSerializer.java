package com.github.nginate.kafka.network;

import com.github.nginate.kafka.exceptions.SerializationException;
import com.github.nginate.kafka.network.client.BinaryClientContext;
import io.netty.buffer.ByteBuf;

/**
 * In case if we'll ever want to replace our current implementation or allow override it explicitly
 */
public interface BinaryMessageSerializer {
    /**
     * Write message directly to netty byte buffer. Could throw runtime serialization exception
     * @param buf buffer
     * @param message message object
     * @throws SerializationException if any serialization error occurs
     */
    void serialize(ByteBuf buf, Object message) throws SerializationException;

    /**
     * Deserialize response from netty byte buffer
     * @param buf buffer
     * @param clientContext context containing response type and correlation id
     * @return response with type, set within client context
     * @throws SerializationException if any serialization error occurs
     */
    Object deserialize(ByteBuf buf, BinaryClientContext clientContext) throws SerializationException;
}
