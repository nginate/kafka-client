package com.github.nginate.kafka.protocol.messages;

import com.github.nginate.kafka.protocol.types.Type;
import com.github.nginate.kafka.protocol.types.TypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.nginate.kafka.protocol.types.TypeName.*;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class Response extends Message {
    @Type(INT32)
    private int correlationId;
}
