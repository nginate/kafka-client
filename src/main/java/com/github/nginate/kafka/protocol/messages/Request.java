package com.github.nginate.kafka.protocol.messages;

import com.github.nginate.kafka.protocol.types.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.nginate.kafka.protocol.types.TypeName.INT16;
import static com.github.nginate.kafka.protocol.types.TypeName.INT32;
import static com.github.nginate.kafka.protocol.types.TypeName.STRING;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class Request extends Message{
    @Type(INT16)
    private int apiKey;
    @Type(INT16)
    private int apiVersion;
    @Type(INT32)
    private int correlationId;
    @Type(STRING)
    private String clientId;
}
