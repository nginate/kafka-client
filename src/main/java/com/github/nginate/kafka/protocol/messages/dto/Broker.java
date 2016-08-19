package com.github.nginate.kafka.protocol.messages.dto;

import com.github.nginate.kafka.serialization.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.github.nginate.kafka.serialization.TypeName.INT32;
import static com.github.nginate.kafka.serialization.TypeName.STRING;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Broker {
    @Type(INT32)
    private Integer nodeId;
    @Type(value = STRING, order = 1)
    private String host;
    @Type(value = INT32, order = 2)
    private Integer port;
    @Type(value = STRING, order = 3)
    private String rack;
}
