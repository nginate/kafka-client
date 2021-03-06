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
public class TopicAndPartition {
    @Type(STRING)
    private String topic;
    @Type(value = INT32, order = 1)
    private Integer partition;
}
