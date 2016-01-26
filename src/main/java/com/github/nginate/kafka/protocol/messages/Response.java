package com.github.nginate.kafka.protocol.messages;

import com.github.nginate.kafka.network.AnswerableMessage;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Data;

import static com.github.nginate.kafka.protocol.types.TypeName.INT32;

/**
 * Field order starts with 1 because of Message size field
 */
@Data
public class Response implements AnswerableMessage<Integer> {
    @Type(value = INT32, order = 1)
    private Integer correlationId;
    private Object message;
}
