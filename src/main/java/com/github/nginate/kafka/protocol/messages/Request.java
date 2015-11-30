package com.github.nginate.kafka.protocol.messages;

import com.github.nginate.kafka.network.AnswerableMessage;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.nginate.kafka.protocol.types.TypeName.INT16;
import static com.github.nginate.kafka.protocol.types.TypeName.INT32;
import static com.github.nginate.kafka.protocol.types.TypeName.STRING;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class Request extends Message implements AnswerableMessage {
    @Type(INT16)
    private Short apiKey;
    @Type(INT16)
    private Short apiVersion;
    @Type(INT32)
    private Integer correlationId;
    @Type(STRING)
    private String clientId;
}
