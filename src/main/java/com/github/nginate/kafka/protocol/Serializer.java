package com.github.nginate.kafka.protocol;

import com.github.nginate.kafka.exceptions.SerializationException;
import com.github.nginate.kafka.protocol.messages.Request;
import com.github.nginate.kafka.protocol.messages.Response;

public class Serializer {

    public <RQ extends Request> byte[] serialize(RQ request) throws SerializationException {
        return null;
    }

    public <RS extends Response> RS deserialize(byte[] rawResponse) throws SerializationException{
        return null;
    }
}
