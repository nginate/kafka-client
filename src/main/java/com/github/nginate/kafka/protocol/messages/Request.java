package com.github.nginate.kafka.protocol.messages;

import com.github.nginate.kafka.network.AnswerableMessage;
import com.github.nginate.kafka.serialization.Type;
import lombok.Builder;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.*;

/**
 * Field order starts with 1 because of Message size field
 */
@Data
@Builder
public class Request implements AnswerableMessage<Integer> {
    @Type(value = INT16, order = 1)
    private Short apiVersion;
    @Type(value = INT32, order = 2)
    private Integer correlationId;
    @Type(value = STRING, order = 3)
    private String clientId;
    private Object message;
}
