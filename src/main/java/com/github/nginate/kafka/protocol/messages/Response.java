package com.github.nginate.kafka.protocol.messages;

import com.github.nginate.kafka.network.AnswerableMessage;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.nginate.kafka.protocol.types.TypeName.INT32;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class Response extends Message implements AnswerableMessage<Integer> {
    @Type(INT32)
    private Integer correlationId;
}
