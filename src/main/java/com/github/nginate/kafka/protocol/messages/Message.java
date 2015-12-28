package com.github.nginate.kafka.protocol.messages;

import com.github.nginate.kafka.protocol.types.Type;
import lombok.Data;

import static com.github.nginate.kafka.protocol.types.TypeName.INT32;

@Data
public abstract class Message {
    @Type(INT32)
    private Integer size;
}
