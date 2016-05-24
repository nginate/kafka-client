package com.github.nginate.kafka.protocol.messages;

import com.github.nginate.kafka.network.AnswerableMessage;
import com.github.nginate.kafka.serialization.Type;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.INT32;

/**
 * Field order starts with 1 because of Message size field
 */
@Data
public class Response implements AnswerableMessage<Integer> {
    @Type(INT32)
    private Integer correlationId;
    private Object message;
}
